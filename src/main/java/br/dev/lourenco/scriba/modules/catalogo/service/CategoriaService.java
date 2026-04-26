package br.dev.lourenco.scriba.modules.catalogo.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.catalogo.domain.Categoria;
import br.dev.lourenco.scriba.modules.catalogo.dto.CategoriaForm;
import br.dev.lourenco.scriba.modules.catalogo.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final TenantContext tenantContext;

    public CategoriaService(CategoriaRepository categoriaRepository, TenantContext tenantContext) {
        this.categoriaRepository = categoriaRepository;
        this.tenantContext = tenantContext;
    }

    public List<Categoria> listarDaInstituicaoAtual(String busca) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (StringUtils.hasText(busca)) {
            return categoriaRepository.findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(instituicaoId, busca);
        }
        return categoriaRepository.findAllByInstituicaoIdOrderByNomeAsc(instituicaoId);
    }

    public Categoria buscarDaInstituicaoAtual(UUID id) {
        return categoriaRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    public CategoriaForm paraFormulario(Categoria categoria) {
        CategoriaForm form = new CategoriaForm();
        form.setNome(categoria.getNome());
        form.setDescricao(categoria.getDescricao());
        form.setPaiId(categoria.getPai() != null ? categoria.getPai().getId() : null);
        form.setAtivo(categoria.isAtivo());
        return form;
    }

    @Transactional
    public Categoria criar(CategoriaForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (categoriaRepository.existsByNomeIgnoreCaseAndInstituicaoId(form.getNome(), instituicaoId)) {
            throw new BusinessException("Já existe categoria com este nome nesta instituição.");
        }
        Categoria categoria = new Categoria();
        categoria.setInstituicaoId(instituicaoId);
        aplicarFormulario(categoria, form, instituicaoId, null);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(UUID id, CategoriaForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Categoria categoria = buscarDaInstituicaoAtual(id);
        if (categoriaRepository.existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(form.getNome(), instituicaoId, id)) {
            throw new BusinessException("Já existe categoria com este nome nesta instituição.");
        }
        aplicarFormulario(categoria, form, instituicaoId, id);
        return categoria;
    }

    private void aplicarFormulario(Categoria categoria, CategoriaForm form, UUID instituicaoId, UUID idAtual) {
        categoria.setNome(form.getNome());
        categoria.setDescricao(form.getDescricao());
        categoria.setAtivo(form.isAtivo());
        categoria.setPai(resolverPai(form.getPaiId(), instituicaoId, idAtual));
    }

    private Categoria resolverPai(UUID paiId, UUID instituicaoId, UUID idAtual) {
        if (paiId == null) {
            return null;
        }
        if (paiId.equals(idAtual)) {
            throw new BusinessException("Categoria não pode ser pai de si mesma.");
        }
        Categoria pai = categoriaRepository.findByIdAndInstituicaoId(paiId, instituicaoId)
            .orElseThrow(() -> new BusinessException("Categoria pai não pertence à instituição atual."));
        if (pai.getPai() != null) {
            throw new BusinessException("Categoria pai não pode ser uma subcategoria.");
        }
        return pai;
    }
}
