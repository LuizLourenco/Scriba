package br.dev.lourenco.scriba.modules.acervo.dto;

import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import jakarta.validation.constraints.NotNull;

public class MudarStatusForm {

    @NotNull
    private StatusAcervo novoStatus;

    private String justificativa;

    public StatusAcervo getNovoStatus() {
        return novoStatus;
    }

    public void setNovoStatus(StatusAcervo novoStatus) {
        this.novoStatus = novoStatus;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }
}
