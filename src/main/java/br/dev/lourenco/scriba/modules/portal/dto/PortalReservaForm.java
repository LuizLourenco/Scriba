package br.dev.lourenco.scriba.modules.portal.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class PortalReservaForm {

    @NotNull
    private UUID acervoItemId;

    public UUID getAcervoItemId() {
        return acervoItemId;
    }

    public void setAcervoItemId(UUID acervoItemId) {
        this.acervoItemId = acervoItemId;
    }
}
