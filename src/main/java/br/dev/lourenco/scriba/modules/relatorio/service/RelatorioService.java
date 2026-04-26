package br.dev.lourenco.scriba.modules.relatorio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusEmprestimo;
import br.dev.lourenco.scriba.modules.circulacao.repository.EmprestimoRepository;
import br.dev.lourenco.scriba.modules.relatorio.dto.EmprestimosEmAbertoRelatorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final EmprestimoRepository emprestimoRepository;
    private final TenantContext tenantContext;

    public RelatorioService(EmprestimoRepository emprestimoRepository, TenantContext tenantContext) {
        this.emprestimoRepository = emprestimoRepository;
        this.tenantContext = tenantContext;
    }

    public EmprestimosEmAbertoRelatorio emprestimosEmAberto(LocalDate de, LocalDate ate) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        LocalDate inicio = de != null ? de : LocalDate.now().minusMonths(1);
        LocalDate fim = ate != null ? ate : LocalDate.now();
        List<Emprestimo> emprestimos = emprestimoRepository
            .findAllByInstituicaoIdAndStatusAndDataEmprestimoBetweenOrderByDataEmprestimoDesc(
                instituicaoId,
                StatusEmprestimo.ATIVO,
                inicio,
                fim
            );
        long total = emprestimoRepository.countByInstituicaoIdAndStatusAndDataEmprestimoBetween(
            instituicaoId,
            StatusEmprestimo.ATIVO,
            inicio,
            fim
        );
        return new EmprestimosEmAbertoRelatorio(inicio, fim, total, emprestimos);
    }
}
