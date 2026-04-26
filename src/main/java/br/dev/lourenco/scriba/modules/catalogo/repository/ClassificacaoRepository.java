package br.dev.lourenco.scriba.modules.catalogo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.tenant.TenantScopedRepository;
import br.dev.lourenco.scriba.modules.catalogo.domain.Classificacao;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantScopedRepository
public interface ClassificacaoRepository extends JpaRepository<Classificacao, UUID> {

    List<Classificacao> findAllByInstituicaoIdOrderByCodigoAsc(UUID instituicaoId);

    List<Classificacao> findAllByInstituicaoIdAndDescricaoContainingIgnoreCaseOrderByCodigoAsc(UUID instituicaoId, String descricao);

    Optional<Classificacao> findByIdAndInstituicaoId(UUID id, UUID instituicaoId);

    boolean existsByCodigoIgnoreCaseAndInstituicaoId(String codigo, UUID instituicaoId);

    boolean existsByCodigoIgnoreCaseAndInstituicaoIdAndIdNot(String codigo, UUID instituicaoId, UUID id);
}
