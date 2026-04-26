package br.dev.lourenco.scriba.core;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AuthenticationIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void autenticaComCredenciaisValidas() throws Exception {
        mvc.perform(post("/login")
                .with(csrf())
                .param("email", "admin@scriba.dev")
                .param("senha", "admin123"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void rejeitaCredenciaisInvalidas() throws Exception {
        mvc.perform(post("/login")
                .with(csrf())
                .param("email", "admin@scriba.dev")
                .param("senha", "errada"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/login?error"));
    }
}
