package br.dev.lourenco.scriba.modules.curadoria.dto;

import java.util.UUID;

import br.dev.lourenco.scriba.modules.curadoria.domain.TipoDesbastamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DesbastamentoForm {

    @NotNull
    private UUID acervoItemId;

    @NotNull
    private TipoDesbastamento tipo;

    private UUID destinoBibliotecaId;

    @NotBlank(message = "Justificativa é obrigatória")
    @Size(max = 1000)
    private String justificativa;

    public UUID getAcervoItemId() {
        return acervoItemId;
    }

    public void setAcervoItemId(UUID acervoItemId) {
        this.acervoItemId = acervoItemId;
    }

    public TipoDesbastamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDesbastamento tipo) {
        this.tipo = tipo;
    }

    public UUID getDestinoBibliotecaId() {
        return destinoBibliotecaId;
    }

    public void setDestinoBibliotecaId(UUID destinoBibliotecaId) {
        this.destinoBibliotecaId = destinoBibliotecaId;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }
}
