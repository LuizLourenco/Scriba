package br.dev.lourenco.scriba.modules.administracao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface BibliotecaRepository extends JpaRepository<Biblioteca, UUID> {

    List<Biblioteca> findAllByInstituicaoId(UUID instituicaoId);

    List<Biblioteca> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    Optional<Biblioteca> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByCodigoIgnoreCaseAndInstituicaoId(String codigo, UUID instituicaoId);

    boolean existsByCodigoIgnoreCaseAndInstituicaoIdAndIdNot(String codigo, UUID instituicaoId, UUID id);
}
