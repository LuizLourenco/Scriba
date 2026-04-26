package br.dev.lourenco.scriba.modules.pessoas.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.pessoas.domain.Fornecedor;
import br.dev.lourenco.scriba.modules.pessoas.dto.FornecedorForm;
import br.dev.lourenco.scriba.modules.pessoas.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final TenantContext tenantContext;

    public FornecedorService(FornecedorRepository fornecedorRepository, TenantContext tenantContext) {
        this.fornecedorRepository = fornecedorRepository;
        this.tenantContext = tenantContext;
    }

    public List<Fornecedor> listarDaInstituicaoAtual() {
        return fornecedorRepository.findAllByInstituicaoIdOrderByNomeAsc(tenantContext.requireInstituicaoId());
    }

    public Fornecedor buscarDaInstituicaoAtual(UUID id) {
        return fornecedorRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado"));
    }

    public FornecedorForm paraFormulario(Fornecedor fornecedor) {
        FornecedorForm form = new FornecedorForm();
        form.setNome(fornecedor.getNome());
        form.setTipo(fornecedor.getTipo());
        form.setCpfCnpj(fornecedor.getCpfCnpj());
        form.setEmail(fornecedor.getEmail());
        form.setTelefone(fornecedor.getTelefone());
        form.setAtivo(fornecedor.isAtivo());
        return form;
    }

    @Transactional
    public Fornecedor criar(FornecedorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (fornecedorRepository.existsByCpfCnpjAndInstituicaoId(form.getCpfCnpj(), instituicaoId)) {
            throw new BusinessException("Já existe fornecedor com este documento nesta instituição.");
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setInstituicaoId(instituicaoId);
        aplicarFormulario(fornecedor, form);
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizar(UUID id, FornecedorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Fornecedor fornecedor = buscarDaInstituicaoAtual(id);
        if (fornecedorRepository.existsByCpfCnpjAndInstituicaoIdAndIdNot(form.getCpfCnpj(), instituicaoId, id)) {
            throw new BusinessException("Já existe fornecedor com este documento nesta instituição.");
        }
        aplicarFormulario(fornecedor, form);
        return fornecedor;
    }

    private void aplicarFormulario(Fornecedor fornecedor, FornecedorForm form) {
        fornecedor.setNome(form.getNome());
        fornecedor.setTipo(form.getTipo());
        fornecedor.setCpfCnpj(form.getCpfCnpj());
        fornecedor.setEmail(form.getEmail());
        fornecedor.setTelefone(form.getTelefone());
        fornecedor.setAtivo(form.isAtivo());
    }
}
