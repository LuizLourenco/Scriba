package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AdministracaoBibliotecaIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void adminCriaBibliotecaNoTenantAtual() throws Exception {
        UserDetailsImpl admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");
        String codigo = "BIB-" + java.util.UUID.randomUUID();

        mvc.perform(post("/admin/bibliotecas")
                .with(csrf())
                .with(user(admin))
                .param("nome", "Biblioteca Bairro")
                .param("codigo", codigo)
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/bibliotecas"));

        Biblioteca biblioteca = bibliotecaRepository.findAllByInstituicaoId(admin.getInstituicaoId())
            .stream()
            .filter(item -> codigo.equals(item.getCodigo()))
            .findFirst()
            .orElseThrow();

        assertThat(biblioteca.getInstituicaoId()).isEqualTo(admin.getInstituicaoId());
    }

    @Test
    void adminListaBibliotecasDaInstituicaoAtual() throws Exception {
        UserDetailsImpl admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");

        mvc.perform(get("/admin/bibliotecas").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Biblioteca Central")));
    }
}
