package br.dev.lourenco.scriba.modules.pessoas.dto;

import br.dev.lourenco.scriba.modules.pessoas.domain.TipoFornecedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FornecedorForm {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotNull
    private TipoFornecedor tipo = TipoFornecedor.PJ;

    @NotBlank
    @Size(max = 18)
    private String cpfCnpj;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 30)
    private String telefone;

    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoFornecedor getTipo() {
        return tipo;
    }

    public void setTipo(TipoFornecedor tipo) {
        this.tipo = tipo;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
