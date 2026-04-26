package br.dev.lourenco.scriba.core;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class RbacIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void adminAcessaRotaAdministrativa() throws Exception {
        mvc.perform(get("/admin/usuarios")
                .with(user(userDetailsService.loadUserByUsername("admin@scriba.dev"))))
            .andExpect(status().isOk());
    }

    @Test
    void leitorRecebeForbiddenEmRotaAdministrativa() throws Exception {
        mvc.perform(get("/admin/usuarios").with(user("leitor").roles("LEITOR")))
            .andExpect(status().isForbidden());
    }

    @Test
    void bibliotecarioAcessaCatalogoMasNaoAdmin() throws Exception {
        mvc.perform(get("/catalogo/autores")
                .with(user(userDetailsService.loadUserByUsername("bibliotecario@scriba.dev"))))
            .andExpect(status().isOk());

        mvc.perform(get("/admin/usuarios")
                .with(user(userDetailsService.loadUserByUsername("bibliotecario@scriba.dev"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void actuatorExigeAdminSemRedirecionarParaLogin() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/actuator/health")
                .with(user(userDetailsService.loadUserByUsername("bibliotecario@scriba.dev"))))
            .andExpect(status().isForbidden());

        mvc.perform(get("/actuator/health")
                .with(user(userDetailsService.loadUserByUsername("admin@scriba.dev"))))
            .andExpect(status().isOk());
    }
}
