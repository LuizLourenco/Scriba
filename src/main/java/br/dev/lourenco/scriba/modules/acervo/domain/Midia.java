package br.dev.lourenco.scriba.modules.acervo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "acervo_midia")
@DiscriminatorValue("MIDIA")
public class Midia extends AcervoItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_midia", length = 20)
    private TipoMidia tipoMidia = TipoMidia.CD;

    @Column(name = "duracao", length = 50)
    private String duracao;

    @Column(name = "produtora", length = 150)
    private String produtora;

    @Override
    public TipoItem tipo() {
        return TipoItem.MIDIA;
    }

    public TipoMidia getTipoMidia() {
        return tipoMidia;
    }

    public void setTipoMidia(TipoMidia tipoMidia) {
        this.tipoMidia = tipoMidia;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public String getProdutora() {
        return produtora;
    }

    public void setProdutora(String produtora) {
        this.produtora = produtora;
    }
}
