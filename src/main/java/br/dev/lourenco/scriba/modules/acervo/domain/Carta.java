package br.dev.lourenco.scriba.modules.acervo.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "acervo_carta")
@DiscriminatorValue("CARTA")
public class Carta extends AcervoItem {

    @Column(name = "remetente", length = 150)
    private String remetente;

    @Column(name = "destinatario", length = 150)
    private String destinatario;

    @Column(name = "data_envio")
    private LocalDate dataEnvio;

    @Override
    public TipoItem tipo() {
        return TipoItem.CARTA;
    }

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}
