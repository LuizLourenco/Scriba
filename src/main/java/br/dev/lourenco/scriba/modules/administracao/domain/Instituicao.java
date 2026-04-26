package br.dev.lourenco.scriba.modules.administracao.domain;

import br.dev.lourenco.scriba.core.domain.BaseEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "instituicao")
public class Instituicao extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "codigo", nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Embedded
    private RegraEmprestimo regraEmprestimo = new RegraEmprestimo();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public RegraEmprestimo getRegraEmprestimo() {
        return regraEmprestimo;
    }

    public void setRegraEmprestimo(RegraEmprestimo regraEmprestimo) {
        this.regraEmprestimo = regraEmprestimo;
    }
}
