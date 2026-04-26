package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.Livro;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.curadoria.domain.Desbastamento;
import br.dev.lourenco.scriba.modules.curadoria.domain.TipoDesbastamento;
import br.dev.lourenco.scriba.modules.curadoria.dto.DesbastamentoForm;
import br.dev.lourenco.scriba.modules.curadoria.repository.DesbastamentoRepository;
import br.dev.lourenco.scriba.modules.curadoria.service.CuradoriaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

class CuradoriaIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CuradoriaService curadoriaService;

    @Autowired
    private AcervoItemRepository acervoItemRepository;

    @Autowired
    private DesbastamentoRepository desbastamentoRepository;

    private UserDetailsImpl admin;
    private UserDetailsImpl bibliotecario;

    @BeforeEach
    void carregarUsuarios() {
        admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");
        bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
    }

    @AfterEach
    void limparSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminDescartaItemElegivelComAuditoria() throws Exception {
        AcervoItem item = criarLivro(StatusAcervo.DISPONIVEL);

        mvc.perform(post("/curadoria/desbastamentos")
                .with(csrf())
                .with(user(admin))
                .param("acervoItemId", item.getId().toString())
                .param("tipo", TipoDesbastamento.DESCARTE.name())
                .param("justificativa", "Item danificado sem recuperacao"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/curadoria/desbastamentos"));

        AcervoItem descartado = acervoItemRepository.findByIdAndInstituicaoId(item.getId(), admin.getInstituicaoId()).orElseThrow();
        assertThat(descartado.getStatus()).isEqualTo(StatusAcervo.DESCARTADO);
        assertThat(descartado.getDeletedAt()).isNotNull();

        Desbastamento desbastamento = desbastamentoRepository.findAllByInstituicaoIdOrderByCriadoEmDesc(admin.getInstituicaoId())
            .stream()
            .filter(registro -> registro.getAcervoItem().getId().equals(item.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(desbastamento.getUsuario().getId()).isEqualTo(admin.getId());
        assertThat(desbastamento.getInstituicaoId()).isEqualTo(admin.getInstituicaoId());
        assertThat(desbastamento.getTipo()).isEqualTo(TipoDesbastamento.DESCARTE);
        assertThat(desbastamento.getJustificativa()).isEqualTo("Item danificado sem recuperacao");
        assertThat(desbastamento.getCriadoEm()).isNotNull();
    }

    @Test
    void bibliotecarioRecebeForbiddenNaRotaEAccessDeniedNoService() throws Exception {
        AcervoItem item = criarLivro(StatusAcervo.DISPONIVEL);
        DesbastamentoForm form = form(item.getId(), TipoDesbastamento.DESCARTE, null, "Teste");

        mvc.perform(post("/curadoria/desbastamentos")
                .with(csrf())
                .with(user(bibliotecario))
                .param("acervoItemId", item.getId().toString())
                .param("tipo", TipoDesbastamento.DESCARTE.name())
                .param("justificativa", "Teste"))
            .andExpect(status().isForbidden());

        autenticar(bibliotecario);
        assertThatThrownBy(() -> curadoriaService.registrar(form))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void bloqueiaDescarteDeItemEmprestadoOuReservado() {
        autenticar(admin);
        AcervoItem emprestado = criarLivro(StatusAcervo.EMPRESTADO);
        AcervoItem reservado = criarLivro(StatusAcervo.RESERVADO);

        assertThatThrownBy(() -> curadoriaService.registrar(form(
            emprestado.getId(),
            TipoDesbastamento.DESCARTE,
            null,
            "Teste"
        )))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("EMPRESTADO");

        assertThatThrownBy(() -> curadoriaService.registrar(form(
            reservado.getId(),
            TipoDesbastamento.DESCARTE,
            null,
            "Teste"
        )))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("RESERVADO");
    }

    @Test
    void justificativaEmBrancoERejeitada() {
        autenticar(admin);
        AcervoItem item = criarLivro(StatusAcervo.DISPONIVEL);

        assertThatThrownBy(() -> curadoriaService.registrar(form(item.getId(), TipoDesbastamento.DESCARTE, null, " ")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Justificativa é obrigatória");
    }

    @Test
    void remanejamentoRegistraDestino() {
        autenticar(admin);
        AcervoItem item = criarLivro(StatusAcervo.DISPONIVEL);
        Biblioteca destino = bibliotecaRepository.findAllByInstituicaoIdOrderByNomeAsc(admin.getInstituicaoId()).stream()
            .findFirst()
            .orElseThrow();

        Desbastamento desbastamento = curadoriaService.registrar(form(
            item.getId(),
            TipoDesbastamento.REMANEJAMENTO,
            destino.getId(),
            "Filial solicitou"
        ));

        assertThat(acervoItemRepository.findByIdAndInstituicaoId(item.getId(), admin.getInstituicaoId()).orElseThrow().getStatus())
            .isEqualTo(StatusAcervo.REMANEJADO);
        assertThat(desbastamento.getDestinoBiblioteca().getId()).isEqualTo(destino.getId());
        assertThat(desbastamento.getDestinoInstituicaoId()).isEqualTo(admin.getInstituicaoId());
    }

    private void autenticar(UserDetailsImpl usuario) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            usuario,
            usuario.getPassword(),
            usuario.getAuthorities()
        ));
    }

    private AcervoItem criarLivro(StatusAcervo status) {
        Livro livro = new Livro();
        livro.setInstituicaoId(admin.getInstituicaoId());
        livro.setTitulo("Livro Curadoria " + UUID.randomUUID());
        livro.setTombo("CUR-" + UUID.randomUUID());
        livro.setStatus(status);
        return acervoItemRepository.save(livro);
    }

    private DesbastamentoForm form(
        UUID acervoItemId,
        TipoDesbastamento tipo,
        UUID destinoBibliotecaId,
        String justificativa
    ) {
        DesbastamentoForm form = new DesbastamentoForm();
        form.setAcervoItemId(acervoItemId);
        form.setTipo(tipo);
        form.setDestinoBibliotecaId(destinoBibliotecaId);
        form.setJustificativa(justificativa);
        return form;
    }
}
