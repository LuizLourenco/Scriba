package br.dev.lourenco.scriba.modules.administracao.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.dto.BibliotecaForm;
import br.dev.lourenco.scriba.modules.administracao.repository.BibliotecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BibliotecaService {

    private final BibliotecaRepository bibliotecaRepository;
    private final TenantContext tenantContext;

    public BibliotecaService(BibliotecaRepository bibliotecaRepository, TenantContext tenantContext) {
        this.bibliotecaRepository = bibliotecaRepository;
        this.tenantContext = tenantContext;
    }

    public List<Biblioteca> listarDaInstituicaoAtual() {
        return bibliotecaRepository.findAllByInstituicaoIdOrderByNomeAsc(tenantContext.requireInstituicaoId());
    }

    public Biblioteca buscarDaInstituicaoAtual(UUID id) {
        return bibliotecaRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Biblioteca não encontrada"));
    }

    public BibliotecaForm paraFormulario(Biblioteca biblioteca) {
        BibliotecaForm form = new BibliotecaForm();
        form.setNome(biblioteca.getNome());
        form.setCodigo(biblioteca.getCodigo());
        form.setAtivo(biblioteca.isAtivo());
        return form;
    }

    @Transactional
    public Biblioteca criar(BibliotecaForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (bibliotecaRepository.existsByCodigoIgnoreCaseAndInstituicaoId(form.getCodigo(), instituicaoId)) {
            throw new BusinessException("Já existe biblioteca com este código nesta instituição.");
        }

        Biblioteca biblioteca = new Biblioteca();
        biblioteca.setInstituicaoId(instituicaoId);
        aplicarFormulario(biblioteca, form);
        return bibliotecaRepository.save(biblioteca);
    }

    @Transactional
    public Biblioteca atualizar(UUID id, BibliotecaForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Biblioteca biblioteca = buscarDaInstituicaoAtual(id);
        if (bibliotecaRepository.existsByCodigoIgnoreCaseAndInstituicaoIdAndIdNot(form.getCodigo(), instituicaoId, id)) {
            throw new BusinessException("Já existe biblioteca com este código nesta instituição.");
        }
        aplicarFormulario(biblioteca, form);
        return biblioteca;
    }

    @Transactional
    public void alterarAtivo(UUID id, boolean ativo) {
        Biblioteca biblioteca = buscarDaInstituicaoAtual(id);
        biblioteca.setAtivo(ativo);
    }

    private void aplicarFormulario(Biblioteca biblioteca, BibliotecaForm form) {
        biblioteca.setNome(form.getNome());
        biblioteca.setCodigo(form.getCodigo());
        biblioteca.setAtivo(form.isAtivo());
    }
}
