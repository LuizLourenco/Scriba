package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.Livro;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusEmprestimo;
import br.dev.lourenco.scriba.modules.circulacao.repository.EmprestimoRepository;
import br.dev.lourenco.scriba.modules.circulacao.service.CirculacaoService;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import br.dev.lourenco.scriba.modules.pessoas.repository.TipoLeitorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class EmprestimoConcorrenciaTest extends AbstractCoreIntegrationTest {

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

    private UserDetailsImpl bibliotecario;

    @BeforeEach
    void carregarUsuario() {
        bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        autenticar(bibliotecario);
    }

    @AfterEach
    void limparSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void apenasUmEmprestimoAtivoPodeSerCriadoParaMesmoItem() throws Exception {
        AcervoItem item = criarLivro();
        Leitor primeiro = criarLeitor();
        Leitor segundo = criarLeitor();

        Callable<Boolean> emprestarPrimeiro = () -> tentarEmprestar(item.getId(), primeiro.getId());
        Callable<Boolean> emprestarSegundo = () -> tentarEmprestar(item.getId(), segundo.getId());

        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Boolean> resultados = executor.invokeAll(List.of(emprestarPrimeiro, emprestarSegundo))
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .toList();

            assertThat(resultados).containsExactlyInAnyOrder(true, false);
        }

        assertThat(emprestimoRepository.countByLeitorIdAndInstituicaoIdAndStatus(
            primeiro.getId(),
            bibliotecario.getInstituicaoId(),
            StatusEmprestimo.ATIVO
        ) + emprestimoRepository.countByLeitorIdAndInstituicaoIdAndStatus(
            segundo.getId(),
            bibliotecario.getInstituicaoId(),
            StatusEmprestimo.ATIVO
        )).isEqualTo(1);
    }

    private Boolean tentarEmprestar(UUID itemId, UUID leitorId) {
        autenticar(bibliotecario);
        try {
            circulacaoService.realizarEmprestimo(itemId, leitorId);
            return true;
        } catch (RuntimeException ex) {
            return false;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void autenticar(UserDetailsImpl usuario) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            usuario,
            usuario.getPassword(),
            usuario.getAuthorities()
        ));
    }

    private AcervoItem criarLivro() {
        Livro livro = new Livro();
        livro.setInstituicaoId(bibliotecario.getInstituicaoId());
        livro.setTitulo("Livro Concorrencia " + UUID.randomUUID());
        livro.setTombo("CONC-" + UUID.randomUUID());
        livro.setStatus(StatusAcervo.DISPONIVEL);
        return acervoItemRepository.save(livro);
    }

    private Leitor criarLeitor() {
        TipoLeitor tipo = new TipoLeitor();
        tipo.setInstituicaoId(bibliotecario.getInstituicaoId());
        tipo.setNome("Tipo Concorrencia " + UUID.randomUUID());
        tipo = tipoLeitorRepository.save(tipo);

        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Leitor leitor = new Leitor();
        leitor.setInstituicaoId(bibliotecario.getInstituicaoId());
        leitor.setTipoLeitor(tipo);
        leitor.setNome("Leitor Concorrencia " + sufixo);
        leitor.setCpf("CC-" + sufixo);
        leitor.setEmail("concorrencia-" + sufixo + "@scriba.dev");
        leitor.setAtivo(true);
        return leitorRepository.save(leitor);
    }
}
