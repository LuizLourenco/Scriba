package br.dev.lourenco.scriba.modules.circulacao.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class ReservaForm {

    @NotNull
    private UUID acervoItemId;

    @NotNull
    private UUID leitorId;

    public UUID getAcervoItemId() {
        return acervoItemId;
    }

    public void setAcervoItemId(UUID acervoItemId) {
        this.acervoItemId = acervoItemId;
    }

    public UUID getLeitorId() {
        return leitorId;
    }

    public void setLeitorId(UUID leitorId) {
        this.leitorId = leitorId;
    }
}
