package br.dev.lourenco.scriba.modules.pessoas.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface LeitorRepository extends JpaRepository<Leitor, UUID> {

    @EntityGraph(attributePaths = "tipoLeitor")
    List<Leitor> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    Optional<Leitor> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByCpfAndInstituicaoId(String cpf, UUID instituicaoId);

    boolean existsByCpfAndInstituicaoIdAndIdNot(String cpf, UUID instituicaoId, UUID id);

    boolean existsByEmailIgnoreCaseAndInstituicaoId(String email, UUID instituicaoId);

    boolean existsByEmailIgnoreCaseAndInstituicaoIdAndIdNot(String email, UUID instituicaoId, UUID id);
}
