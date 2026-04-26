package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lourenco.scriba.core.exception.TenantIsolationViolationException;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

class TenantIsolationCoreTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void sessaoDaInstituicaoAtualNaoRetornaUsuariosDeOutroTenant() throws Exception {
        mvc.perform(get("/admin/usuarios")
                .with(user(userDetailsService.loadUserByUsername("admin@scriba.dev"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("admin@scriba.dev")))
            .andExpect(content().string(not(containsString(usuarioOutroTenant.getEmail()))));
    }

    @Test
    void findAllSemTenantExplodeEmModoEstrito() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new br.dev.lourenco.scriba.core.security.UserDetailsImpl(
                    usuarioRepository.findByEmailIgnoreCase("admin@scriba.dev").orElseThrow()
                ),
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );

        assertThatThrownBy(() -> usuarioRepository.findAll())
            .isInstanceOf(TenantIsolationViolationException.class)
            .hasMessageContaining("instituicaoId");
    }
}
