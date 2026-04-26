package br.dev.lourenco.scriba.core;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class GlobalExceptionHandlerIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void retornaProblemDetailParaRequisicaoNaoHtmx() throws Exception {
        mvc.perform(get("/catalogo/autores/{id}", UUID.randomUUID())
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void retornaFragmentoHtmlParaRequisicaoHtmx() throws Exception {
        mvc.perform(get("/catalogo/autores/{id}", UUID.randomUUID())
                .header("HX-Request", "true")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("alert-error")));
    }
}
