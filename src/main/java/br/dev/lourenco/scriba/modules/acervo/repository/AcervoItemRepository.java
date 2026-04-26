package br.dev.lourenco.scriba.modules.acervo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface AcervoItemRepository extends JpaRepository<AcervoItem, UUID> {

    @EntityGraph(attributePaths = "biblioteca")
    List<AcervoItem> findAllByInstituicaoIdOrderByTituloAsc(UUID instituicaoId);

    @EntityGraph(attributePaths = "biblioteca")
    List<AcervoItem> findAllByInstituicaoIdAndTituloContainingIgnoreCaseOrderByTituloAsc(UUID instituicaoId, String titulo);

    Optional<AcervoItem> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByTomboIgnoreCaseAndInstituicaoId(String tombo, UUID instituicaoId);

    long countByInstituicaoIdAndStatus(UUID instituicaoId, StatusAcervo status);
}
