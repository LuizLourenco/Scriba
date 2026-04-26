package br.dev.lourenco.scriba.modules.circulacao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import br.dev.lourenco.scriba.modules.administracao.domain.RegraEmprestimo;
import br.dev.lourenco.scriba.modules.administracao.domain.TipoMulta;
import br.dev.lourenco.scriba.modules.administracao.repository.InstituicaoRepository;
import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.Multa;
import br.dev.lourenco.scriba.modules.circulacao.domain.Reserva;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusEmprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusMulta;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusReserva;
import br.dev.lourenco.scriba.modules.circulacao.repository.EmprestimoRepository;
import br.dev.lourenco.scriba.modules.circulacao.repository.MultaRepository;
import br.dev.lourenco.scriba.modules.circulacao.repository.ReservaRepository;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CirculacaoService {

    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;
    private final MultaRepository multaRepository;
    private final AcervoItemRepository acervoItemRepository;
    private final LeitorRepository leitorRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final TenantContext tenantContext;

    public CirculacaoService(
        EmprestimoRepository emprestimoRepository,
        ReservaRepository reservaRepository,
        MultaRepository multaRepository,
        AcervoItemRepository acervoItemRepository,
        LeitorRepository leitorRepository,
        InstituicaoRepository instituicaoRepository,
        TenantContext tenantContext
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.multaRepository = multaRepository;
        this.acervoItemRepository = acervoItemRepository;
        this.leitorRepository = leitorRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.tenantContext = tenantContext;
    }

    public List<Emprestimo> listarEmprestimosDaInstituicaoAtual() {
        return emprestimoRepository.findAllByInstituicaoIdOrderByDataEmprestimoDesc(tenantContext.requireInstituicaoId());
    }

    public List<Reserva> listarReservasDaInstituicaoAtual() {
        return reservaRepository.findAllByInstituicaoIdOrderByDataReservaDesc(tenantContext.requireInstituicaoId());
    }

    public List<Multa> listarMultasDaInstituicaoAtual() {
        return multaRepository.findAllByInstituicaoIdOrderByDataGeracaoDesc(tenantContext.requireInstituicaoId());
    }

    @Transactional
    public Emprestimo realizarEmprestimo(UUID acervoItemId, UUID leitorId) {
        try {
            return criarEmprestimo(acervoItemId, leitorId);
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new BusinessException("Item já possui empréstimo ativo.");
        }
    }

    @Transactional
    public Reserva reservar(UUID acervoItemId, UUID leitorId) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        RegraEmprestimo regra = regraAtual(instituicaoId);
        AcervoItem item = buscarItem(acervoItemId, instituicaoId);
        Leitor leitor = buscarLeitor(leitorId, instituicaoId);

        if (reservaRepository.countByLeitorIdAndInstituicaoIdAndStatus(leitorId, instituicaoId, StatusReserva.AGUARDANDO)
            >= regra.getLimiteReservas()) {
            throw new BusinessException("Leitor atingiu o limite de reservas simultâneas.");
        }
        if (reservaRepository.findByAcervoItemIdAndLeitorIdAndInstituicaoIdAndStatus(
            acervoItemId,
            leitorId,
            instituicaoId,
            StatusReserva.AGUARDANDO
        ).isPresent()) {
            throw new BusinessException("Leitor já possui reserva ativa para este item.");
        }
        if (!List.of(StatusAcervo.DISPONIVEL, StatusAcervo.RESERVADO, StatusAcervo.EMPRESTADO).contains(item.getStatus())) {
            throw new BusinessException("Item não está disponível para reserva.");
        }

        long aguardando = reservaRepository.countByAcervoItemIdAndInstituicaoIdAndStatus(
            acervoItemId,
            instituicaoId,
            StatusReserva.AGUARDANDO
        );
        if (item.getStatus() == StatusAcervo.DISPONIVEL) {
            item.mudarStatus(StatusAcervo.RESERVADO);
        }

        LocalDate hoje = LocalDate.now();
        Reserva reserva = new Reserva();
        reserva.setInstituicaoId(instituicaoId);
        reserva.setAcervoItem(item);
        reserva.setLeitor(leitor);
        reserva.setStatus(StatusReserva.AGUARDANDO);
        reserva.setDataReserva(hoje);
        reserva.setDataExpiracao(hoje.plusDays(regra.getDiasExpiracaoReserva()));
        reserva.setPosicaoFila((int) aguardando + 1);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Emprestimo renovar(UUID emprestimoId) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        RegraEmprestimo regra = regraAtual(instituicaoId);
        Emprestimo emprestimo = buscarEmprestimo(emprestimoId, instituicaoId);

        if (emprestimo.getStatus() != StatusEmprestimo.ATIVO) {
            throw new BusinessException("Apenas empréstimos ativos podem ser renovados.");
        }
        if (emprestimo.getRenovacoes() >= regra.getMaximoRenovacoes()) {
            throw new BusinessException("Limite de renovações atingido.");
        }
        if (emprestimo.estaAtrasado(LocalDate.now())) {
            throw new BusinessException("Empréstimo atrasado não pode ser renovado.");
        }

        emprestimo.renovar(regra.getPrazoPadraoDias());
        return emprestimo;
    }

    @Transactional
    public Emprestimo devolver(UUID emprestimoId) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        RegraEmprestimo regra = regraAtual(instituicaoId);
        Emprestimo emprestimo = buscarEmprestimo(emprestimoId, instituicaoId);

        if (emprestimo.getStatus() != StatusEmprestimo.ATIVO) {
            throw new BusinessException("Empréstimo já foi devolvido.");
        }

        LocalDate hoje = LocalDate.now();
        if (emprestimo.estaAtrasado(hoje)) {
            multaRepository.save(criarMulta(emprestimo, regra, hoje, instituicaoId));
        }

        emprestimo.devolver(hoje);
        List<Reserva> fila = reservaRepository.findAllByAcervoItemIdAndInstituicaoIdAndStatusOrderByPosicaoFilaAsc(
            emprestimo.getAcervoItem().getId(),
            instituicaoId,
            StatusReserva.AGUARDANDO
        );
        if (fila.isEmpty()) {
            emprestimo.getAcervoItem().mudarStatus(StatusAcervo.DISPONIVEL);
        } else {
            emprestimo.getAcervoItem().mudarStatus(StatusAcervo.RESERVADO);
        }
        return emprestimo;
    }

    private Emprestimo criarEmprestimo(UUID acervoItemId, UUID leitorId) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        RegraEmprestimo regra = regraAtual(instituicaoId);
        AcervoItem item = buscarItem(acervoItemId, instituicaoId);
        Leitor leitor = buscarLeitor(leitorId, instituicaoId);

        validarLeitorPodeEmprestar(leitorId, instituicaoId, regra);
        Reserva reservaAtiva = reservaRepository.findByAcervoItemIdAndLeitorIdAndInstituicaoIdAndStatus(
            acervoItemId,
            leitorId,
            instituicaoId,
            StatusReserva.AGUARDANDO
        ).orElse(null);
        validarItemPodeSerEmprestado(item, reservaAtiva);

        item.mudarStatus(StatusAcervo.EMPRESTADO);
        if (reservaAtiva != null) {
            reservaAtiva.setStatus(StatusReserva.ATENDIDA);
        }

        LocalDate hoje = LocalDate.now();
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setInstituicaoId(instituicaoId);
        emprestimo.setAcervoItem(item);
        emprestimo.setLeitor(leitor);
        emprestimo.setDataEmprestimo(hoje);
        emprestimo.setDataPrevistaDevolucao(hoje.plusDays(regra.getPrazoPadraoDias()));
        emprestimo.setStatus(StatusEmprestimo.ATIVO);
        return emprestimoRepository.save(emprestimo);
    }

    private void validarLeitorPodeEmprestar(UUID leitorId, UUID instituicaoId, RegraEmprestimo regra) {
        if (regra.isBloqueioComMulta()
            && multaRepository.existsByLeitorIdAndInstituicaoIdAndStatus(leitorId, instituicaoId, StatusMulta.PENDENTE)) {
            throw new BusinessException("Leitor possui multa pendente.");
        }
        long emprestimosAtivos = emprestimoRepository.countByLeitorIdAndInstituicaoIdAndStatus(
            leitorId,
            instituicaoId,
            StatusEmprestimo.ATIVO
        );
        if (emprestimosAtivos >= regra.getLimiteEmprestimos()) {
            throw new BusinessException("Leitor atingiu o limite de empréstimos simultâneos.");
        }
    }

    private void validarItemPodeSerEmprestado(AcervoItem item, Reserva reservaAtiva) {
        if (item.getStatus() == StatusAcervo.DISPONIVEL) {
            return;
        }
        if (item.getStatus() == StatusAcervo.RESERVADO && reservaAtiva != null) {
            return;
        }
        throw new BusinessException("Item não está disponível para empréstimo.");
    }

    private Multa criarMulta(Emprestimo emprestimo, RegraEmprestimo regra, LocalDate hoje, UUID instituicaoId) {
        int diasAtraso = (int) ChronoUnit.DAYS.between(emprestimo.getDataPrevistaDevolucao(), hoje);
        BigDecimal valor = regra.getTipoMulta() == TipoMulta.PERCENTUAL
            ? regra.getValorMulta().multiply(BigDecimal.valueOf(diasAtraso)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            : regra.getValorMulta().multiply(BigDecimal.valueOf(diasAtraso));
        if (regra.getTetoMaximoMulta() != null && valor.compareTo(regra.getTetoMaximoMulta()) > 0) {
            valor = regra.getTetoMaximoMulta();
        }

        Multa multa = new Multa();
        multa.setInstituicaoId(instituicaoId);
        multa.setEmprestimo(emprestimo);
        multa.setLeitor(emprestimo.getLeitor());
        multa.setDiasAtraso(diasAtraso);
        multa.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        multa.setStatus(StatusMulta.PENDENTE);
        multa.setDataGeracao(hoje);
        return multa;
    }

    private RegraEmprestimo regraAtual(UUID instituicaoId) {
        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Instituição não encontrada"));
        return instituicao.getRegraEmprestimo();
    }

    private AcervoItem buscarItem(UUID acervoItemId, UUID instituicaoId) {
        return acervoItemRepository.findByIdAndInstituicaoId(acervoItemId, instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Item do acervo não encontrado"));
    }

    private Leitor buscarLeitor(UUID leitorId, UUID instituicaoId) {
        Leitor leitor = leitorRepository.findByIdAndInstituicaoId(leitorId, instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Leitor não encontrado"));
        if (!leitor.isAtivo()) {
            throw new BusinessException("Leitor inativo não pode realizar circulação.");
        }
        return leitor;
    }

    private Emprestimo buscarEmprestimo(UUID emprestimoId, UUID instituicaoId) {
        return emprestimoRepository.findByIdAndInstituicaoId(emprestimoId, instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado"));
    }
}
