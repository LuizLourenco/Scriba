package br.dev.lourenco.scriba.modules.acervo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "acervo_livro")
@DiscriminatorValue("LIVRO")
public class Livro extends AcervoItem {

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(name = "edicao", length = 50)
    private String edicao;

    @Column(name = "volume", length = 50)
    private String volume;

    @Override
    public TipoItem tipo() {
        return TipoItem.LIVRO;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getEdicao() {
        return edicao;
    }

    public void setEdicao(String edicao) {
        this.edicao = edicao;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }
}
