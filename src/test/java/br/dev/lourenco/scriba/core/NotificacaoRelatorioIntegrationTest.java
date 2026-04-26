package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.Livro;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusEmprestimo;
import br.dev.lourenco.scriba.modules.circulacao.repository.EmprestimoRepository;
import br.dev.lourenco.scriba.modules.circulacao.service.CirculacaoService;
import br.dev.lourenco.scriba.modules.notificacao.service.NotificacaoService;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import br.dev.lourenco.scriba.modules.pessoas.repository.TipoLeitorRepository;
import br.dev.lourenco.scriba.modules.relatorio.dto.EmprestimosEmAbertoRelatorio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class NotificacaoRelatorioIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CirculacaoService circulacaoService;

    @Autowired
    private AcervoItemRepository acervoItemRepository;

    @Autowired
    private TipoLeitorRepository tipoLeitorRepository;

    @Autowired
    private LeitorRepository leitorRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @MockitoBean
    private NotificacaoService notificacaoService;

    private UserDetailsImpl admin;

    @BeforeEach
    void autenticarAdmin() {
        admin = (UserDetailsImpl) userDetailsService.loadUserByUsername("admin@scriba.dev");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            admin,
            admin.getPassword(),
            admin.getAuthorities()
        ));
        configurarRegras();
        when(notificacaoService.notificarAtraso(any(Emprestimo.class))).thenReturn(true);
    }

    @AfterEach
    void limparSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jobManualNotificaEmprestimosAtrasados() throws Exception {
        Emprestimo emprestimo = criarEmprestimoAtivo();
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(2));
        emprestimoRepository.save(emprestimo);

        mvc.perform(post("/admin/jobs/notificar-atrasos")
                .with(csrf())
                .with(user(admin)))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/relatorios/emprestimos-em-aberto"));

        verify(notificacaoService, atLeastOnce()).notificarAtraso(any(Emprestimo.class));
    }

    @Test
    void relatorioDeEmprestimosEmAbertoConfereComRepositorio() throws Exception {
        criarEmprestimoAtivo();
        LocalDate de = LocalDate.now().minusDays(1);
        LocalDate ate = LocalDate.now().plusDays(1);
        long esperado = emprestimoRepository.countByInstituicaoIdAndStatusAndDataEmprestimoBetween(
            admin.getInstituicaoId(),
            StatusEmprestimo.ATIVO,
            de,
            ate
        );

        mvc.perform(get("/admin/relatorios/emprestimos-em-aberto")
                .with(user(admin))
                .param("de", de.toString())
                .param("ate", ate.toString()))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("relatorio"))
            .andExpect(result -> {
                EmprestimosEmAbertoRelatorio relatorio = (EmprestimosEmAbertoRelatorio) result.getModelAndView()
                    .getModel()
                    .get("relatorio");
                assertThat(relatorio.total()).isEqualTo(esperado);
            });
    }

    private void configurarRegras() {
        Instituicao instituicao = instituicaoRepository.findById(admin.getInstituicaoId()).orElseThrow();
        instituicao.getRegraEmprestimo().setLimiteEmprestimos(100);
        instituicao.getRegraEmprestimo().setValorMulta(BigDecimal.ZERO);
        instituicaoRepository.save(instituicao);
    }

    private Emprestimo criarEmprestimoAtivo() {
        return circulacaoService.realizarEmprestimo(criarLivro().getId(), criarLeitor().getId());
    }

    private AcervoItem criarLivro() {
        Livro livro = new Livro();
        livro.setInstituicaoId(admin.getInstituicaoId());
        livro.setTitulo("Livro Relatorio " + UUID.randomUUID());
        livro.setTombo("REL-" + UUID.randomUUID());
        livro.setStatus(StatusAcervo.DISPONIVEL);
        return acervoItemRepository.save(livro);
    }

    private Leitor criarLeitor() {
        TipoLeitor tipo = new TipoLeitor();
        tipo.setInstituicaoId(admin.getInstituicaoId());
        tipo.setNome("Tipo Relatorio " + UUID.randomUUID());
        tipo = tipoLeitorRepository.save(tipo);

        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Leitor leitor = new Leitor();
        leitor.setInstituicaoId(admin.getInstituicaoId());
        leitor.setTipoLeitor(tipo);
        leitor.setNome("Leitor Relatorio " + sufixo);
        leitor.setCpf("RT-" + sufixo);
        leitor.setEmail("relatorio-" + sufixo + "@scriba.dev");
        leitor.setAtivo(true);
        return leitorRepository.save(leitor);
    }
}
