package br.dev.lourenco.scriba.modules.pessoas.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.dto.LeitorForm;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import br.dev.lourenco.scriba.modules.pessoas.repository.TipoLeitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LeitorService {

    private final LeitorRepository leitorRepository;
    private final TipoLeitorRepository tipoLeitorRepository;
    private final TenantContext tenantContext;

    public LeitorService(
        LeitorRepository leitorRepository,
        TipoLeitorRepository tipoLeitorRepository,
        TenantContext tenantContext
    ) {
        this.leitorRepository = leitorRepository;
        this.tipoLeitorRepository = tipoLeitorRepository;
        this.tenantContext = tenantContext;
    }

    public List<Leitor> listarDaInstituicaoAtual() {
        return leitorRepository.findAllByInstituicaoIdOrderByNomeAsc(tenantContext.requireInstituicaoId());
    }

    public Leitor buscarDaInstituicaoAtual(UUID id) {
        return leitorRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Leitor não encontrado"));
    }

    public LeitorForm paraFormulario(Leitor leitor) {
        LeitorForm form = new LeitorForm();
        form.setNome(leitor.getNome());
        form.setCpf(leitor.getCpf());
        form.setEmail(leitor.getEmail());
        form.setTelefone(leitor.getTelefone());
        form.setTipoLeitorId(leitor.getTipoLeitor().getId());
        form.setAtivo(leitor.isAtivo());
        return form;
    }

    @Transactional
    public Leitor criar(LeitorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        validarUnicos(form, instituicaoId, null);

        Leitor leitor = new Leitor();
        leitor.setInstituicaoId(instituicaoId);
        aplicarFormulario(leitor, form, instituicaoId);
        return leitorRepository.save(leitor);
    }

    @Transactional
    public Leitor atualizar(UUID id, LeitorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Leitor leitor = buscarDaInstituicaoAtual(id);
        validarUnicos(form, instituicaoId, id);
        aplicarFormulario(leitor, form, instituicaoId);
        return leitor;
    }

    private void validarUnicos(LeitorForm form, UUID instituicaoId, UUID idAtual) {
        if (idAtual == null && leitorRepository.existsByCpfAndInstituicaoId(form.getCpf(), instituicaoId)) {
            throw new BusinessException("Já existe leitor com este CPF nesta instituição.");
        }
        if (idAtual == null && leitorRepository.existsByEmailIgnoreCaseAndInstituicaoId(form.getEmail(), instituicaoId)) {
            throw new BusinessException("Já existe leitor com este e-mail nesta instituição.");
        }
        if (idAtual != null && leitorRepository.existsByCpfAndInstituicaoIdAndIdNot(form.getCpf(), instituicaoId, idAtual)) {
            throw new BusinessException("Já existe leitor com este CPF nesta instituição.");
        }
        if (idAtual != null && leitorRepository.existsByEmailIgnoreCaseAndInstituicaoIdAndIdNot(form.getEmail(), instituicaoId, idAtual)) {
            throw new BusinessException("Já existe leitor com este e-mail nesta instituição.");
        }
    }

    private void aplicarFormulario(Leitor leitor, LeitorForm form, UUID instituicaoId) {
        leitor.setNome(form.getNome());
        leitor.setCpf(form.getCpf());
        leitor.setEmail(form.getEmail());
        leitor.setTelefone(form.getTelefone());
        leitor.setAtivo(form.isAtivo());
        leitor.setTipoLeitor(resolverTipoLeitor(form.getTipoLeitorId(), instituicaoId));
    }

    private TipoLeitor resolverTipoLeitor(UUID tipoLeitorId, UUID instituicaoId) {
        return tipoLeitorRepository.findByIdAndInstituicaoId(tipoLeitorId, instituicaoId)
            .orElseThrow(() -> new BusinessException("Tipo de leitor não pertence à instituição atual."));
    }
}
