package br.dev.lourenco.scriba.modules.catalogo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.catalogo.domain.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface AutorRepository extends JpaRepository<Autor, UUID> {

    List<Autor> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    List<Autor> findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(UUID instituicaoId, String nome);

    Optional<Autor> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoId(String nome, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(String nome, UUID instituicaoId, UUID id);
}
