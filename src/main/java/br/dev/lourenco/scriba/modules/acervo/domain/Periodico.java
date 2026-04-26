package br.dev.lourenco.scriba.modules.acervo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "acervo_periodico")
@DiscriminatorValue("PERIODICO")
public class Periodico extends AcervoItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_periodico", length = 20)
    private TipoPeriodico tipoPeriodico = TipoPeriodico.REVISTA;

    @Column(name = "issn", length = 20)
    private String issn;

    @Column(name = "volume", length = 50)
    private String volume;

    @Column(name = "numero", length = 50)
    private String numero;

    @Override
    public TipoItem tipo() {
        return TipoItem.PERIODICO;
    }

    public TipoPeriodico getTipoPeriodico() {
        return tipoPeriodico;
    }

    public void setTipoPeriodico(TipoPeriodico tipoPeriodico) {
        this.tipoPeriodico = tipoPeriodico;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
