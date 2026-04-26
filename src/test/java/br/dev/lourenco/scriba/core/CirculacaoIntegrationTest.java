package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.Livro;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import br.dev.lourenco.scriba.modules.administracao.domain.RegraEmprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.Multa;
import br.dev.lourenco.scriba.modules.circulacao.domain.Reserva;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusEmprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusMulta;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusReserva;
import br.dev.lourenco.scriba.modules.circulacao.repository.EmprestimoRepository;
import br.dev.lourenco.scriba.modules.circulacao.repository.MultaRepository;
import br.dev.lourenco.scriba.modules.circulacao.repository.ReservaRepository;
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

class CirculacaoIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CirculacaoService circulacaoService;

    @Autowired
    private AcervoItemRepository acervoItemRepository;

    @Autowired
    private LeitorRepository leitorRepository;

    @Autowired
    private TipoLeitorRepository tipoLeitorRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MultaRepository multaRepository;

    private UserDetailsImpl bibliotecario;

    @BeforeEach
    void autenticarBibliotecario() {
        bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        autenticar(bibliotecario);
        configurarRegrasPadrao();
    }

    @AfterEach
    void limparSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void realizaEmprestimoEAtualizaStatusDoItem() {
        AcervoItem item = criarLivro();
        Leitor leitor = criarLeitor();

        Emprestimo emprestimo = circulacaoService.realizarEmprestimo(item.getId(), leitor.getId());

        assertThat(emprestimo.getStatus()).isEqualTo(StatusEmprestimo.ATIVO);
        assertThat(emprestimo.getDataPrevistaDevolucao()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(acervoItemRepository.findByIdAndInstituicaoId(item.getId(), bibliotecario.getInstituicaoId()).orElseThrow().getStatus())
            .isEqualTo(StatusAcervo.EMPRESTADO);
    }

    @Test
    void reservaMantemFilaFifoEPermiteEmprestimoAoPrimeiroLeitor() {
        AcervoItem item = criarLivro();
        Leitor primeiro = criarLeitor();
        Leitor segundo = criarLeitor();

        Reserva primeiraReserva = circulacaoService.reservar(item.getId(), primeiro.getId());
        Reserva segundaReserva = circulacaoService.reservar(item.getId(), segundo.getId());
        Emprestimo emprestimo = circulacaoService.realizarEmprestimo(item.getId(), primeiro.getId());

        assertThat(primeiraReserva.getPosicaoFila()).isEqualTo(1);
        assertThat(segundaReserva.getPosicaoFila()).isEqualTo(2);
        assertThat(emprestimo.getLeitor().getId()).isEqualTo(primeiro.getId());
        assertThat(reservaRepository.findByAcervoItemIdAndLeitorIdAndInstituicaoIdAndStatus(
            item.getId(),
            primeiro.getId(),
            bibliotecario.getInstituicaoId(),
            StatusReserva.ATENDIDA
        )).isPresent();
    }

    @Test
    void renovacaoRespeitaLimiteConfigurado() {
        configurarMaximoRenovacoes(1);
        Emprestimo emprestimo = circulacaoService.realizarEmprestimo(criarLivro().getId(), criarLeitor().getId());

        circulacaoService.renovar(emprestimo.getId());

        assertThat(emprestimoRepository.findByIdAndInstituicaoId(emprestimo.getId(), bibliotecario.getInstituicaoId()).orElseThrow()
            .getRenovacoes()).isEqualTo(1);
        assertThatThrownBy(() -> circulacaoService.renovar(emprestimo.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Limite de renovações");
    }

    @Test
    void devolucaoAtrasadaGeraMultaEDeixaItemDisponivel() {
        configurarValorMulta(BigDecimal.valueOf(2));
        AcervoItem item = criarLivro();
        Emprestimo emprestimo = circulacaoService.realizarEmprestimo(item.getId(), criarLeitor().getId());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(3));
        emprestimoRepository.save(emprestimo);

        circulacaoService.devolver(emprestimo.getId());

        Multa multa = multaRepository.findAllByInstituicaoIdOrderByDataGeracaoDesc(bibliotecario.getInstituicaoId()).stream()
            .filter(itemMulta -> itemMulta.getEmprestimo().getId().equals(emprestimo.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(multa.getStatus()).isEqualTo(StatusMulta.PENDENTE);
        assertThat(multa.getDiasAtraso()).isEqualTo(3);
        assertThat(multa.getValor()).isEqualByComparingTo("6.00");
        assertThat(acervoItemRepository.findByIdAndInstituicaoId(item.getId(), bibliotecario.getInstituicaoId()).orElseThrow().getStatus())
            .isEqualTo(StatusAcervo.DISPONIVEL);
    }

    private void autenticar(UserDetailsImpl usuario) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            usuario,
            usuario.getPassword(),
            usuario.getAuthorities()
        ));
    }

    private void configurarRegrasPadrao() {
        Instituicao instituicao = instituicaoRepository.findById(bibliotecario.getInstituicaoId()).orElseThrow();
        RegraEmprestimo regra = instituicao.getRegraEmprestimo();
        regra.setPrazoPadraoDias(14);
        regra.setLimiteEmprestimos(3);
        regra.setLimiteReservas(3);
        regra.setDiasExpiracaoReserva(7);
        regra.setMaximoRenovacoes(3);
        regra.setValorMulta(BigDecimal.ZERO);
        regra.setBloqueioComMulta(true);
        regra.setTetoMaximoMulta(null);
        instituicaoRepository.save(instituicao);
    }

    private void configurarMaximoRenovacoes(int maximoRenovacoes) {
        Instituicao instituicao = instituicaoRepository.findById(bibliotecario.getInstituicaoId()).orElseThrow();
        instituicao.getRegraEmprestimo().setMaximoRenovacoes(maximoRenovacoes);
        instituicaoRepository.save(instituicao);
    }

    private void configurarValorMulta(BigDecimal valorMulta) {
        Instituicao instituicao = instituicaoRepository.findById(bibliotecario.getInstituicaoId()).orElseThrow();
        instituicao.getRegraEmprestimo().setValorMulta(valorMulta);
        instituicaoRepository.save(instituicao);
    }

    private AcervoItem criarLivro() {
        Livro livro = new Livro();
        livro.setInstituicaoId(bibliotecario.getInstituicaoId());
        livro.setTitulo("Livro Circulacao " + UUID.randomUUID());
        livro.setTombo("CIRC-" + UUID.randomUUID());
        livro.setStatus(StatusAcervo.DISPONIVEL);
        return acervoItemRepository.save(livro);
    }

    private Leitor criarLeitor() {
        TipoLeitor tipo = new TipoLeitor();
        tipo.setInstituicaoId(bibliotecario.getInstituicaoId());
        tipo.setNome("Tipo Circulacao " + UUID.randomUUID());
        tipo = tipoLeitorRepository.save(tipo);

        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Leitor leitor = new Leitor();
        leitor.setInstituicaoId(bibliotecario.getInstituicaoId());
        leitor.setTipoLeitor(tipo);
        leitor.setNome("Leitor Circulacao " + sufixo);
        leitor.setCpf("CPF-" + sufixo);
        leitor.setEmail("leitor-" + sufixo + "@scriba.dev");
        leitor.setAtivo(true);
        return leitorRepository.save(leitor);
    }
}
