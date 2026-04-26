package br.dev.lourenco.scriba.modules.catalogo.domain;

import br.dev.lourenco.scriba.core.domain.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "autor")
public class Autor extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "nacionalidade", length = 100)
    private String nacionalidade;

    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
