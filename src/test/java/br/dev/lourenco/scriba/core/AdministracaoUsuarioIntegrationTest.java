package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.administracao.domain.Role;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AdministracaoUsuarioIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void adminCriaUsuarioNoTenantAtualComSenhaBCrypt() throws Exception {
        UserDetailsImpl admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");
        String email = "maria-" + java.util.UUID.randomUUID() + "@scriba.dev";

        mvc.perform(post("/admin/usuarios")
                .with(csrf())
                .with(user(admin))
                .param("nome", "Maria")
                .param("email", email)
                .param("senha", "Temp123")
                .param("role", Role.BIBLIOTECARIO.name())
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/usuarios"));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(usuario.getInstituicaoId()).isEqualTo(admin.getInstituicaoId());
        assertThat(usuario.getSenha()).startsWith("$2a$12$");
        assertThat(usuario.getSenha()).hasSize(60);
    }

    @Test
    void listagemDeUsuariosNaoMostraOutroTenant() throws Exception {
        UserDetailsImpl admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");

        mvc.perform(get("/admin/usuarios").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("admin@scriba.dev")))
            .andExpect(content().string(not(containsString(usuarioOutroTenant.getEmail()))));
    }

    @Test
    void adminDesativaUsuarioDoTenantAtual() throws Exception {
        UserDetailsImpl admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");
        String email = "desativar-" + java.util.UUID.randomUUID() + "@scriba.dev";

        mvc.perform(post("/admin/usuarios")
                .with(csrf())
                .with(user(admin))
                .param("nome", "Usuário Temporário")
                .param("email", email)
                .param("senha", "Temp123")
                .param("role", Role.BIBLIOTECARIO.name())
                .param("ativo", "true"))
            .andExpect(status().isFound());

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();

        mvc.perform(patch("/admin/usuarios/{id}/desativar", usuario.getId())
                .with(csrf())
                .with(user(admin)))
            .andExpect(status().isOk());

        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow().isAtivo()).isFalse();
    }
}
