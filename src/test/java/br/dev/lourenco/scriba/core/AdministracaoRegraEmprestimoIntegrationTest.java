package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AdministracaoRegraEmprestimoIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void adminAtualizaRegrasDeEmprestimoDaInstituicaoAtual() throws Exception {
        UserDetailsImpl admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");

        mvc.perform(post("/admin/regras-emprestimo")
                .with(csrf())
                .with(user(admin))
                .param("prazoPadraoDias", "21")
                .param("limiteEmprestimos", "7")
                .param("valorMulta", "1.00"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/regras-emprestimo"));

        Instituicao instituicao = instituicaoRepository.findById(admin.getInstituicaoId()).orElseThrow();
        assertThat(instituicao.getRegraEmprestimo().getPrazoPadraoDias()).isEqualTo(21);
        assertThat(instituicao.getRegraEmprestimo().getLimiteEmprestimos()).isEqualTo(7);
        assertThat(instituicao.getRegraEmprestimo().getValorMulta()).isEqualByComparingTo(new BigDecimal("1.00"));
    }
}
