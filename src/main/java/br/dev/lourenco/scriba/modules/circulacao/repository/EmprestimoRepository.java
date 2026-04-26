package br.dev.lourenco.scriba.modules.circulacao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.StatusEmprestimo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, UUID> {

    @EntityGraph(attributePaths = {"acervoItem", "leitor"})
    List<Emprestimo> findAllByInstituicaoIdOrderByDataEmprestimoDesc(UUID instituicaoId);

    @EntityGraph(attributePaths = {"acervoItem", "leitor"})
    List<Emprestimo> findAllByLeitorIdAndInstituicaoIdOrderByDataEmprestimoDesc(UUID leitorId, UUID instituicaoId);

    @EntityGraph(attributePaths = {"acervoItem", "leitor"})
    List<Emprestimo> findAllByInstituicaoIdAndStatusAndDataEmprestimoBetweenOrderByDataEmprestimoDesc(
        UUID instituicaoId,
        StatusEmprestimo status,
        LocalDate de,
        LocalDate ate
    );

    @EntityGraph(attributePaths = {"acervoItem", "leitor"})
    List<Emprestimo> findAllByInstituicaoIdAndStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(
        UUID instituicaoId,
        StatusEmprestimo status,
        LocalDate dataReferencia
    );

    @EntityGraph(attributePaths = {"acervoItem", "leitor"})
    Optional<Emprestimo> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    Optional<Emprestimo> findByAcervoItemIdAndInstituicaoIdAndStatus(
        UUID acervoItemId,
        UUID instituicaoId,
        StatusEmprestimo status
    );

    long countByLeitorIdAndInstituicaoIdAndStatus(UUID leitorId, UUID instituicaoId, StatusEmprestimo status);

    long countByInstituicaoIdAndStatusAndDataEmprestimoBetween(
        UUID instituicaoId,
        StatusEmprestimo status,
        LocalDate de,
        LocalDate ate
    );
}
