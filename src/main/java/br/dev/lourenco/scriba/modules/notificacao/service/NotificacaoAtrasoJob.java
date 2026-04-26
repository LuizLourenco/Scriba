package br.dev.lourenco.scriba.modules.notificacao.service;

import java.time.LocalDate;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusEmprestimo;
import br.dev.lourenco.scriba.modules.circulacao.repository.EmprestimoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificacaoAtrasoJob {

    private final EmprestimoRepository emprestimoRepository;
    private final NotificacaoService notificacaoService;
    private final TenantContext tenantContext;

    public NotificacaoAtrasoJob(
        EmprestimoRepository emprestimoRepository,
        NotificacaoService notificacaoService,
        TenantContext tenantContext
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.notificacaoService = notificacaoService;
        this.tenantContext = tenantContext;
    }

    @Scheduled(cron = "${scriba.notificacoes.atrasos-cron:0 0 8 * * *}")
    public void executarAgendado() {
        tenantContext.instituicaoId().ifPresent(this::executarParaInstituicao);
    }

    public int executarParaInstituicaoAtual() {
        return executarParaInstituicao(tenantContext.requireInstituicaoId());
    }

    private int executarParaInstituicao(UUID instituicaoId) {
        return (int) emprestimoRepository
            .findAllByInstituicaoIdAndStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(
                instituicaoId,
                StatusEmprestimo.ATIVO,
                LocalDate.now()
            )
            .stream()
            .filter(notificacaoService::notificarAtraso)
            .count();
    }
}
