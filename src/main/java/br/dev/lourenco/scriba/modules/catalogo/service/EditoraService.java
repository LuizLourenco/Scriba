package br.dev.lourenco.scriba.modules.catalogo.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.catalogo.domain.Editora;
import br.dev.lourenco.scriba.modules.catalogo.dto.EditoraForm;
import br.dev.lourenco.scriba.modules.catalogo.repository.EditoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class EditoraService {

    private final EditoraRepository editoraRepository;
    private final TenantContext tenantContext;

    public EditoraService(EditoraRepository editoraRepository, TenantContext tenantContext) {
        this.editoraRepository = editoraRepository;
        this.tenantContext = tenantContext;
    }

    public List<Editora> listarDaInstituicaoAtual(String busca) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (StringUtils.hasText(busca)) {
            return editoraRepository.findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(instituicaoId, busca);
        }
        return editoraRepository.findAllByInstituicaoIdOrderByNomeAsc(instituicaoId);
    }

    public Editora buscarDaInstituicaoAtual(UUID id) {
        return editoraRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Editora não encontrada"));
    }

    public EditoraForm paraFormulario(Editora editora) {
        EditoraForm form = new EditoraForm();
        form.setNome(editora.getNome());
        form.setCidade(editora.getCidade());
        form.setPais(editora.getPais());
        form.setAtivo(editora.isAtivo());
        return form;
    }

    @Transactional
    public Editora criar(EditoraForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (editoraRepository.existsByNomeIgnoreCaseAndInstituicaoId(form.getNome(), instituicaoId)) {
            throw new BusinessException("Já existe editora com este nome nesta instituição.");
        }
        Editora editora = new Editora();
        editora.setInstituicaoId(instituicaoId);
        aplicarFormulario(editora, form);
        return editoraRepository.save(editora);
    }

    @Transactional
    public Editora atualizar(UUID id, EditoraForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Editora editora = buscarDaInstituicaoAtual(id);
        if (editoraRepository.existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(form.getNome(), instituicaoId, id)) {
            throw new BusinessException("Já existe editora com este nome nesta instituição.");
        }
        aplicarFormulario(editora, form);
        return editora;
    }

    private void aplicarFormulario(Editora editora, EditoraForm form) {
        editora.setNome(form.getNome());
        editora.setCidade(form.getCidade());
        editora.setPais(form.getPais());
        editora.setAtivo(form.isAtivo());
    }
}
