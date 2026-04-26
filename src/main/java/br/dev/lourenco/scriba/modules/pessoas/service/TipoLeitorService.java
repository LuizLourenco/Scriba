package br.dev.lourenco.scriba.modules.pessoas.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.dto.TipoLeitorForm;
import br.dev.lourenco.scriba.modules.pessoas.repository.TipoLeitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TipoLeitorService {

    private final TipoLeitorRepository tipoLeitorRepository;
    private final TenantContext tenantContext;

    public TipoLeitorService(TipoLeitorRepository tipoLeitorRepository, TenantContext tenantContext) {
        this.tipoLeitorRepository = tipoLeitorRepository;
        this.tenantContext = tenantContext;
    }

    public List<TipoLeitor> listarDaInstituicaoAtual() {
        return tipoLeitorRepository.findAllByInstituicaoIdOrderByNomeAsc(tenantContext.requireInstituicaoId());
    }

    public TipoLeitor buscarDaInstituicaoAtual(UUID id) {
        return tipoLeitorRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de leitor não encontrado"));
    }

    public TipoLeitorForm paraFormulario(TipoLeitor tipoLeitor) {
        TipoLeitorForm form = new TipoLeitorForm();
        form.setNome(tipoLeitor.getNome());
        form.setPrazoPadraoDias(tipoLeitor.getPrazoPadraoDias());
        form.setLimiteEmprestimos(tipoLeitor.getLimiteEmprestimos());
        form.setAtivo(tipoLeitor.isAtivo());
        return form;
    }

    @Transactional
    public TipoLeitor criar(TipoLeitorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (tipoLeitorRepository.existsByNomeIgnoreCaseAndInstituicaoId(form.getNome(), instituicaoId)) {
            throw new BusinessException("Já existe tipo de leitor com este nome nesta instituição.");
        }

        TipoLeitor tipoLeitor = new TipoLeitor();
        tipoLeitor.setInstituicaoId(instituicaoId);
        aplicarFormulario(tipoLeitor, form);
        return tipoLeitorRepository.save(tipoLeitor);
    }

    @Transactional
    public TipoLeitor atualizar(UUID id, TipoLeitorForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        TipoLeitor tipoLeitor = buscarDaInstituicaoAtual(id);
        if (tipoLeitorRepository.existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(form.getNome(), instituicaoId, id)) {
            throw new BusinessException("Já existe tipo de leitor com este nome nesta instituição.");
        }
        aplicarFormulario(tipoLeitor, form);
        return tipoLeitor;
    }

    private void aplicarFormulario(TipoLeitor tipoLeitor, TipoLeitorForm form) {
        tipoLeitor.setNome(form.getNome());
        tipoLeitor.setPrazoPadraoDias(form.getPrazoPadraoDias());
        tipoLeitor.setLimiteEmprestimos(form.getLimiteEmprestimos());
        tipoLeitor.setAtivo(form.isAtivo());
    }
}
