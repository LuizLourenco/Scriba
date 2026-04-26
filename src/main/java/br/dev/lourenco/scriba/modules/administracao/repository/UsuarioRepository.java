package br.dev.lourenco.scriba.modules.administracao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    List<Usuario> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    Optional<Usuario> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByEmailIgnoreCaseAndInstituicaoId(String email, UUID instituicaoId);

    boolean existsByEmailIgnoreCaseAndInstituicaoIdAndIdNot(String email, UUID instituicaoId, UUID id);
}
