package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.Carta;
import br.dev.lourenco.scriba.modules.acervo.domain.Foto;
import br.dev.lourenco.scriba.modules.acervo.domain.Livro;
import br.dev.lourenco.scriba.modules.acervo.domain.Midia;
import br.dev.lourenco.scriba.modules.acervo.domain.Periodico;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.domain.TipoItem;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AcervoIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AcervoItemRepository acervoItemRepository;

    @Test
    void criaLivroComStatusInicialDisponivelEVersao() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        String tombo = "LIV-" + UUID.randomUUID();

        mvc.perform(post("/acervo/itens")
                .with(csrf())
                .with(user(bibliotecario))
                .param("tipoItem", TipoItem.LIVRO.name())
                .param("titulo", "Dom Casmurro")
                .param("tombo", tombo)
                .param("isbn", "9788525406958")
                .param("numeroPaginas", "256"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/acervo/itens"));

        AcervoItem item = buscarPorTombo(bibliotecario, tombo);
        assertThat(item).isInstanceOf(Livro.class);
        assertThat(item.getStatus()).isEqualTo(StatusAcervo.DISPONIVEL);
        assertThat(item.getVersao()).isNotNull();
        assertThat(((Livro) item).getIsbn()).isEqualTo("9788525406958");
    }

    @Test
    void criaTodosOsSubtiposJoined() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");

        criarItem(bibliotecario, TipoItem.PERIODICO, "PER");
        criarItem(bibliotecario, TipoItem.CARTA, "CAR");
        criarItem(bibliotecario, TipoItem.FOTO, "FOT");
        criarItem(bibliotecario, TipoItem.MIDIA, "MID");

        assertThat(buscarPorPrefixo(bibliotecario, "PER")).isInstanceOf(Periodico.class);
        assertThat(buscarPorPrefixo(bibliotecario, "CAR")).isInstanceOf(Carta.class);
        assertThat(buscarPorPrefixo(bibliotecario, "FOT")).isInstanceOf(Foto.class);
        assertThat(buscarPorPrefixo(bibliotecario, "MID")).isInstanceOf(Midia.class);
    }

    @Test
    void transicaoValidaAtualizaStatus() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        String tombo = criarItem(bibliotecario, TipoItem.LIVRO, "MAN");
        AcervoItem item = buscarPorTombo(bibliotecario, tombo);

        mvc.perform(patch("/acervo/itens/{id}/status", item.getId())
                .with(csrf())
                .with(user(bibliotecario))
                .param("novoStatus", StatusAcervo.EM_MANUTENCAO.name()))
            .andExpect(status().isOk());

        assertThat(acervoItemRepository.findById(item.getId()).orElseThrow().getStatus())
            .isEqualTo(StatusAcervo.EM_MANUTENCAO);
    }

    @Test
    void transicaoInvalidaDeDescartadoParaDisponivelRetorna422EPreservaStatus() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        String tombo = criarItem(bibliotecario, TipoItem.LIVRO, "DES");
        AcervoItem item = buscarPorTombo(bibliotecario, tombo);

        mvc.perform(patch("/acervo/itens/{id}/status", item.getId())
                .with(csrf())
                .with(user(bibliotecario))
                .param("novoStatus", StatusAcervo.DESCARTADO.name()))
            .andExpect(status().isOk());

        mvc.perform(patch("/acervo/itens/{id}/status", item.getId())
                .with(csrf())
                .with(user(bibliotecario))
                .param("novoStatus", StatusAcervo.DISPONIVEL.name()))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string(containsString("não permitida")));

        AcervoItem descartado = acervoItemRepository.findById(item.getId()).orElseThrow();
        assertThat(descartado.getStatus()).isEqualTo(StatusAcervo.DESCARTADO);
        assertThat(descartado.getDeletedAt()).isNotNull();
        assertThat(acervoItemRepository.findAllByInstituicaoIdOrderByTituloAsc(bibliotecario.getInstituicaoId()))
            .filteredOn(acervoItem -> acervoItem.getDeletedAt() != null && acervoItem.getStatus() != StatusAcervo.DESCARTADO)
            .isEmpty();
    }

    private String criarItem(UserDetailsImpl usuario, TipoItem tipoItem, String prefixo) throws Exception {
        String tombo = prefixo + "-" + UUID.randomUUID();
        mvc.perform(post("/acervo/itens")
                .with(csrf())
                .with(user(usuario))
                .param("tipoItem", tipoItem.name())
                .param("titulo", tipoItem + " de teste")
                .param("tombo", tombo))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/acervo/itens"));
        return tombo;
    }

    private AcervoItem buscarPorPrefixo(UserDetailsImpl usuario, String prefixo) {
        return acervoItemRepository.findAllByInstituicaoIdOrderByTituloAsc(usuario.getInstituicaoId())
            .stream()
            .filter(item -> item.getTombo().startsWith(prefixo + "-"))
            .findFirst()
            .orElseThrow();
    }

    private AcervoItem buscarPorTombo(UserDetailsImpl usuario, String tombo) {
        return acervoItemRepository.findAllByInstituicaoIdOrderByTituloAsc(usuario.getInstituicaoId())
            .stream()
            .filter(item -> tombo.equals(item.getTombo()))
            .findFirst()
            .orElseThrow();
    }
}
