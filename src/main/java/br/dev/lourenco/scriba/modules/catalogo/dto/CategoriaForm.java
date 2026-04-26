package br.dev.lourenco.scriba.modules.catalogo.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaForm {

    @NotBlank
    @Size(max = 120)
    private String nome;

    @Size(max = 255)
    private String descricao;

    private UUID paiId;

    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public UUID getPaiId() {
        return paiId;
    }

    public void setPaiId(UUID paiId) {
        this.paiId = paiId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
