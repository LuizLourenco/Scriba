package br.dev.lourenco.scriba.modules.acervo.domain;

import java.util.Set;

import br.dev.lourenco.scriba.core.exception.BusinessException;

public enum StatusAcervo {
    DISPONIVEL,
    RESERVADO,
    EMPRESTADO,
    EM_MANUTENCAO,
    USO_INTERNO,
    EXTRAVIADO,
    REMANEJADO,
    DESCARTADO;

    public StatusAcervo transicionar(StatusAcervo novoStatus) {
        if (!transicoesPermitidas().contains(novoStatus)) {
            throw new BusinessException("Transição " + this + " -> " + novoStatus + " não permitida");
        }
        return novoStatus;
    }

    private Set<StatusAcervo> transicoesPermitidas() {
        return switch (this) {
            case DISPONIVEL -> Set.of(RESERVADO, EMPRESTADO, EM_MANUTENCAO, USO_INTERNO, REMANEJADO, DESCARTADO);
            case RESERVADO -> Set.of(EMPRESTADO, DISPONIVEL);
            case EMPRESTADO -> Set.of(DISPONIVEL, EXTRAVIADO);
            case EM_MANUTENCAO -> Set.of(DISPONIVEL, REMANEJADO, DESCARTADO);
            case USO_INTERNO -> Set.of(DISPONIVEL, EM_MANUTENCAO);
            case EXTRAVIADO -> Set.of(DISPONIVEL, DESCARTADO);
            case REMANEJADO, DESCARTADO -> Set.of();
        };
    }
}
