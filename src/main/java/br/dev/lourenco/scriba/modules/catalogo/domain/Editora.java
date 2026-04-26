package br.dev.lourenco.scriba.modules.catalogo.domain;

import br.dev.lourenco.scriba.core.domain.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "editora")
public class Editora extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
