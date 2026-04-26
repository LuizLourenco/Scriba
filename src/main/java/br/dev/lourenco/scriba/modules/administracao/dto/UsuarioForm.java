package br.dev.lourenco.scriba.modules.administracao.dto;

import br.dev.lourenco.scriba.modules.administracao.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioForm {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(min = 6, max = 72)
    private String senha;

    @NotNull
    private Role role = Role.BIBLIOTECARIO;

    private java.util.UUID bibliotecaId;

    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public java.util.UUID getBibliotecaId() {
        return bibliotecaId;
    }

    public void setBibliotecaId(java.util.UUID bibliotecaId) {
        this.bibliotecaId = bibliotecaId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
