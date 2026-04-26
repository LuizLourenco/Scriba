package br.dev.lourenco.scriba.modules.circulacao.repository;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.circulacao.domain.Multa;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusMulta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface MultaRepository extends JpaRepository<Multa, UUID> {

    @EntityGraph(attributePaths = {"emprestimo", "leitor"})
    List<Multa> findAllByInstituicaoIdOrderByDataGeracaoDesc(UUID instituicaoId);

    @EntityGraph(attributePaths = {"emprestimo", "leitor"})
    List<Multa> findAllByLeitorIdAndInstituicaoIdOrderByDataGeracaoDesc(UUID leitorId, UUID instituicaoId);

    boolean existsByLeitorIdAndInstituicaoIdAndStatus(UUID leitorId, UUID instituicaoId, StatusMulta status);
}
