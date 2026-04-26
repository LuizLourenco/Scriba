package br.dev.lourenco.scriba.modules.administracao.repository;

import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstituicaoRepository extends JpaRepository<Instituicao, UUID> {

    Optional<Instituicao> findByCodigo(String codigo);
}
