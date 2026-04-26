package br.dev.lourenco.scriba.modules.acervo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "acervo_foto")
@DiscriminatorValue("FOTO")
public class Foto extends AcervoItem {

    @Column(name = "assunto", length = 150)
    private String assunto;

    @Column(name = "fotografo", length = 150)
    private String fotografo;

    @Column(name = "formato", length = 80)
    private String formato;

    @Column(name = "resolucao", length = 80)
    private String resolucao;

    @Override
    public TipoItem tipo() {
        return TipoItem.FOTO;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getFotografo() {
        return fotografo;
    }

    public void setFotografo(String fotografo) {
        this.fotografo = fotografo;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getResolucao() {
        return resolucao;
    }

    public void setResolucao(String resolucao) {
        this.resolucao = resolucao;
    }
}
