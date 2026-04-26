package br.dev.lourenco.scriba.modules.curadoria.repository;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.curadoria.domain.Desbastamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface DesbastamentoRepository extends JpaRepository<Desbastamento, UUID> {

    @EntityGraph(attributePaths = {"acervoItem", "usuario", "destinoBiblioteca"})
    List<Desbastamento> findAllByInstituicaoIdOrderByCriadoEmDesc(UUID instituicaoId);
}
