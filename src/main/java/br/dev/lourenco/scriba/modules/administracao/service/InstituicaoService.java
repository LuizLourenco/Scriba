package br.dev.lourenco.scriba.modules.administracao.service;

import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import br.dev.lourenco.scriba.modules.administracao.domain.RegraEmprestimo;
import br.dev.lourenco.scriba.modules.administracao.dto.RegraEmprestimoForm;
import br.dev.lourenco.scriba.modules.administracao.repository.InstituicaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InstituicaoService {

    private final InstituicaoRepository instituicaoRepository;
    private final TenantContext tenantContext;

    public InstituicaoService(InstituicaoRepository instituicaoRepository, TenantContext tenantContext) {
        this.instituicaoRepository = instituicaoRepository;
        this.tenantContext = tenantContext;
    }

    public Instituicao instituicaoAtual() {
        return instituicaoRepository.findById(tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Instituição não encontrada"));
    }

    public RegraEmprestimoForm regrasAtuaisComoFormulario() {
        RegraEmprestimo regra = instituicaoAtual().getRegraEmprestimo();
        RegraEmprestimoForm form = new RegraEmprestimoForm();
        form.setPrazoPadraoDias(regra.getPrazoPadraoDias());
        form.setLimiteEmprestimos(regra.getLimiteEmprestimos());
        form.setValorMulta(regra.getValorMulta());
        return form;
    }

    @Transactional
    public void atualizarRegras(RegraEmprestimoForm form) {
        RegraEmprestimo regra = instituicaoAtual().getRegraEmprestimo();
        regra.setPrazoPadraoDias(form.getPrazoPadraoDias());
        regra.setLimiteEmprestimos(form.getLimiteEmprestimos());
        regra.setValorMulta(form.getValorMulta());
    }
}
