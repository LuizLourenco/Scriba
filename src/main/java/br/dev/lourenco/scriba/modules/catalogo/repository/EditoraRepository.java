package br.dev.lourenco.scriba.modules.catalogo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.catalogo.domain.Editora;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface EditoraRepository extends JpaRepository<Editora, UUID> {

    List<Editora> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    List<Editora> findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(UUID instituicaoId, String nome);

    Optional<Editora> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoId(String nome, UUID instituicaoId);

    boolean existsByNomeIgnoreCaseAndInstituicaoIdAndIdNot(String nome, UUID instituicaoId, UUID id);
}
