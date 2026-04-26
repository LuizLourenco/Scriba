package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.Livro;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.circulacao.domain.Reserva;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusReserva;
import br.dev.lourenco.scriba.modules.circulacao.repository.ReservaRepository;
import br.dev.lourenco.scriba.modules.notificacao.service.NotificacaoService;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import br.dev.lourenco.scriba.modules.pessoas.repository.TipoLeitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class PortalLeitorSegurancaTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AcervoItemRepository acervoItemRepository;

    @Autowired
    private TipoLeitorRepository tipoLeitorRepository;

    @Autowired
    private LeitorRepository leitorRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @MockitoBean
    private NotificacaoService notificacaoService;

    private UserDetailsImpl leitorUsuario;

    @BeforeEach
    void prepararLeitor() {
        leitorUsuario = (UserDetailsImpl) userDetailsService.loadUserByUsername("leitor@scriba.dev");
        Leitor leitor = garantirLeitor(leitorUsuario);
        cancelarReservasAguardando(leitor);
        when(notificacaoService.notificarReservaConfirmada(any(Reserva.class))).thenReturn(true);
    }

    @Test
    void leitorConsultaApenasItensPermitidosDoPortal() throws Exception {
        AcervoItem disponivel = criarLivro("Portal Disponivel", StatusAcervo.DISPONIVEL);
        AcervoItem reservado = criarLivro("Portal Reservado", StatusAcervo.RESERVADO);
        AcervoItem manutencao = criarLivro("Portal Manutencao", StatusAcervo.EM_MANUTENCAO);

        mvc.perform(get("/portal/acervo")
                .with(user(leitorUsuario))
                .param("busca", "Portal"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(disponivel.getTitulo())))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(reservado.getTitulo())))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(manutencao.getTitulo()))));
    }

    @Test
    void leitorNaoAcessaAreaDeBibliotecario() throws Exception {
        mvc.perform(get("/acervo/itens").with(user(leitorUsuario)))
            .andExpect(status().isForbidden());
    }

    @Test
    void reservaPeloPortalDisparaNotificacao() throws Exception {
        AcervoItem item = criarLivro("Portal Reserva " + UUID.randomUUID(), StatusAcervo.DISPONIVEL);
        Leitor leitor = garantirLeitor(leitorUsuario);

        mvc.perform(post("/portal/reservas")
                .with(csrf())
                .with(user(leitorUsuario))
                .param("acervoItemId", item.getId().toString()))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/portal"));

        assertThat(reservaRepository.findByAcervoItemIdAndLeitorIdAndInstituicaoIdAndStatus(
            item.getId(),
            leitor.getId(),
            leitorUsuario.getInstituicaoId(),
            StatusReserva.AGUARDANDO
        )).isPresent();
        verify(notificacaoService).notificarReservaConfirmada(any(Reserva.class));
    }

    private AcervoItem criarLivro(String titulo, StatusAcervo status) {
        Livro livro = new Livro();
        livro.setInstituicaoId(leitorUsuario.getInstituicaoId());
        livro.setTitulo(titulo);
        livro.setTombo("PORT-" + UUID.randomUUID());
        livro.setStatus(status);
        return acervoItemRepository.save(livro);
    }

    private Leitor garantirLeitor(UserDetailsImpl usuario) {
        return leitorRepository.findByEmailIgnoreCaseAndInstituicaoId(usuario.getUsername(), usuario.getInstituicaoId())
            .orElseGet(() -> {
                TipoLeitor tipo = new TipoLeitor();
                tipo.setInstituicaoId(usuario.getInstituicaoId());
                tipo.setNome("Tipo Portal " + UUID.randomUUID());
                tipo = tipoLeitorRepository.save(tipo);

                String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                Leitor leitor = new Leitor();
                leitor.setInstituicaoId(usuario.getInstituicaoId());
                leitor.setTipoLeitor(tipo);
                leitor.setNome("Leitor Portal");
                leitor.setCpf("PL-" + sufixo);
                leitor.setEmail(usuario.getUsername());
                leitor.setAtivo(true);
                return leitorRepository.save(leitor);
            });
    }

    private void cancelarReservasAguardando(Leitor leitor) {
        reservaRepository.findAllByLeitorIdAndInstituicaoIdOrderByDataReservaDesc(leitor.getId(), leitor.getInstituicaoId()).stream()
            .filter(reserva -> reserva.getStatus() == StatusReserva.AGUARDANDO)
            .forEach(reserva -> {
                reserva.setStatus(StatusReserva.CANCELADA);
                reservaRepository.save(reserva);
            });
    }
}
