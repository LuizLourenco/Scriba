package br.dev.lourenco.scriba.modules.catalogo.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.catalogo.domain.Classificacao;
import br.dev.lourenco.scriba.modules.catalogo.dto.ClassificacaoForm;
import br.dev.lourenco.scriba.modules.catalogo.repository.ClassificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ClassificacaoService {

    private final ClassificacaoRepository classificacaoRepository;
    private final TenantContext tenantContext;

    public ClassificacaoService(ClassificacaoRepository classificacaoRepository, TenantContext tenantContext) {
        this.classificacaoRepository = classificacaoRepository;
        this.tenantContext = tenantContext;
    }

    public List<Classificacao> listarDaInstituicaoAtual(String busca) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (StringUtils.hasText(busca)) {
            return classificacaoRepository.findAllByInstituicaoIdAndDescricaoContainingIgnoreCaseOrderByCodigoAsc(instituicaoId, busca);
        }
        return classificacaoRepository.findAllByInstituicaoIdOrderByCodigoAsc(instituicaoId);
    }

    public Classificacao buscarDaInstituicaoAtual(UUID id) {
        return classificacaoRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Classificação não encontrada"));
    }

    public ClassificacaoForm paraFormulario(Classificacao classificacao) {
        ClassificacaoForm form = new ClassificacaoForm();
        form.setPadrao(classificacao.getPadrao());
        form.setCodigo(classificacao.getCodigo());
        form.setDescricao(classificacao.getDescricao());
        form.setAtivo(classificacao.isAtivo());
        return form;
    }

    @Transactional
    public Classificacao criar(ClassificacaoForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (classificacaoRepository.existsByCodigoIgnoreCaseAndInstituicaoId(form.getCodigo(), instituicaoId)) {
            throw new BusinessException("Já existe classificação com este código nesta instituição.");
        }
        Classificacao classificacao = new Classificacao();
        classificacao.setInstituicaoId(instituicaoId);
        aplicarFormulario(classificacao, form);
        return classificacaoRepository.save(classificacao);
    }

    @Transactional
    public Classificacao atualizar(UUID id, ClassificacaoForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Classificacao classificacao = buscarDaInstituicaoAtual(id);
        if (classificacaoRepository.existsByCodigoIgnoreCaseAndInstituicaoIdAndIdNot(form.getCodigo(), instituicaoId, id)) {
            throw new BusinessException("Já existe classificação com este código nesta instituição.");
        }
        aplicarFormulario(classificacao, form);
        return classificacao;
    }

    private void aplicarFormulario(Classificacao classificacao, ClassificacaoForm form) {
        classificacao.setPadrao(form.getPadrao());
        classificacao.setCodigo(form.getCodigo());
        classificacao.setDescricao(form.getDescricao());
        classificacao.setAtivo(form.isAtivo());
    }
}
