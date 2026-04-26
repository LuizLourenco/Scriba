package br.dev.lourenco.scriba.modules.catalogo.domain;

import br.dev.lourenco.scriba.core.domain.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "classificacao")
public class Classificacao extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "padrao", nullable = false, length = 20)
    private PadraoClassificacao padrao = PadraoClassificacao.CDD;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    public PadraoClassificacao getPadrao() {
        return padrao;
    }

    public void setPadrao(PadraoClassificacao padrao) {
        this.padrao = padrao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
