package br.dev.lourenco.scriba.modules.pessoas.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.pessoas.domain.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface FornecedorRepository extends JpaRepository<Fornecedor, UUID> {

    List<Fornecedor> findAllByInstituicaoIdOrderByNomeAsc(UUID instituicaoId);

    Optional<Fornecedor> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByCpfCnpjAndInstituicaoId(String cpfCnpj, UUID instituicaoId);

    boolean existsByCpfCnpjAndInstituicaoIdAndIdNot(String cpfCnpj, UUID instituicaoId, UUID id);
}
