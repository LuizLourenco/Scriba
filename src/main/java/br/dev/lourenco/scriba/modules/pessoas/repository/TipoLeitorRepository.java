package br.dev.lourenco.scriba.modules.pessoas.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface TipoLeitorRepository extends JpaRepository<TipoLeitor, UUID> {

    List<TipoLeitor> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    Optional<TipoLeitor> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoId(String nome, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(String nome, UUID instituicaoId, UUID id);
}
