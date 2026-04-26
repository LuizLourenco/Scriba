package br.dev.lourenco.scriba.modules.circulacao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.circulacao.domain.Reserva;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusReserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    @EntityGraph(attributePaths = {"acervoItem", "leitor"})
    List<Reserva> findAllByInstituicaoIdOrderByDataReservaDesc(UUID instituicaoId);

    @EntityGraph(attributePaths = {"acervoItem", "leitor"})
    List<Reserva> findAllByLeitorIdAndInstituicaoIdOrderByDataReservaDesc(UUID leitorId, UUID instituicaoId);

    Optional<Reserva> findByAcervoItemIdAndLeitorIdAndInstituicaoIdAndStatus(
        UUID acervoItemId,
        UUID leitorId,
        UUID instituicaoId,
        StatusReserva status
    );

    List<Reserva> findAllByAcervoItemIdAndInstituicaoIdAndStatusOrderByPosicaoFilaAsc(
        UUID acervoItemId,
        UUID instituicaoId,
        StatusReserva status
    );

    long countByAcervoItemIdAndInstituicaoIdAndStatus(UUID acervoItemId, UUID instituicaoId, StatusReserva status);

    long countByLeitorIdAndInstituicaoIdAndStatus(UUID leitorId, UUID instituicaoId, StatusReserva status);
}
