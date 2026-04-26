package br.dev.lourenco.scriba.modules.catalogo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.catalogo.domain.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    @EntityGraph(attributePaths = "pai")
    List<Categoria> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    @EntityGraph(attributePaths = "pai")
    List<Categoria> findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(UUID instituicaoId, String nome);

    Optional<Categoria> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoId(String nome, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(String nome, UUID instituicaoId, UUID id);
}
