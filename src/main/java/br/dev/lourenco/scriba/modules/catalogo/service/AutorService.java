package br.dev.lourenco.scriba.modules.catalogo.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.catalogo.domain.Autor;
import br.dev.lourenco.scriba.modules.catalogo.dto.AutorForm;
import br.dev.lourenco.scriba.modules.catalogo.repository.AutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AutorService {

    private final AutorRepository autorRepository;
    private final TenantContext tenantContext;

    public AutorService(AutorRepository autorRepository, TenantContext tenantContext) {
        this.autorRepository = autorRepository;
        this.tenantContext = tenantContext;
    }

    public List<Autor> listarDaInstituicaoAtual(String busca) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (StringUtils.hasText(busca)) {
            return autorRepository.findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(instituicaoId, busca);
        }
        return autorRepository.findAllByInstituicaoIdOrderByNomeAsc(instituicaoId);
    }

    public Autor buscarDaInstituicaoAtual(UUID id) {
        return autorRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado"));
    }

    public AutorForm paraFormulario(Autor autor) {
        AutorForm form = new AutorForm();
        form.setNome(autor.getNome());
        form.setNacionalidade(autor.getNacionalidade());
        form.setBiografia(autor.getBiografia());
        form.setAtivo(autor.isAtivo());
        return form;
    }

    @Transactional
    public Autor criar(AutorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (autorRepository.existsByNomeIgnoreCaseAndInstituicaoId(form.getNome(), instituicaoId)) {
            throw new BusinessException("Já existe autor com este nome nesta instituição.");
        }
        Autor autor = new Autor();
        autor.setInstituicaoId(instituicaoId);
        aplicarFormulario(autor, form);
        return autorRepository.save(autor);
    }

    @Transactional
    public Autor atualizar(UUID id, AutorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Autor autor = buscarDaInstituicaoAtual(id);
        if (autorRepository.existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(form.getNome(), instituicaoId, id)) {
            throw new BusinessException("Já existe autor com este nome nesta instituição.");
        }
        aplicarFormulario(autor, form);
        return autor;
    }

    private void aplicarFormulario(Autor autor, AutorForm form) {
        autor.setNome(form.getNome());
        autor.setNacionalidade(form.getNacionalidade());
        autor.setBiografia(form.getBiografia());
        autor.setAtivo(form.isAtivo());
    }
}
