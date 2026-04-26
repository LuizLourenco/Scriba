package br.dev.lourenco.scriba.modules.catalogo.dto;

import br.dev.lourenco.scriba.modules.catalogo.domain.PadraoClassificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClassificacaoForm {

    @NotNull
    private PadraoClassificacao padrao = PadraoClassificacao.CDD;

    @NotBlank
    @Size(max = 50)
    private String codigo;

    @NotBlank
    @Size(max = 150)
    private String descricao;

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
