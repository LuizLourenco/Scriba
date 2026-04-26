package br.dev.lourenco.scriba.modules.acervo.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.dev.lourenco.scriba.core.domain.TenantEntity;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "acervo_item")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_item", discriminatorType = DiscriminatorType.STRING)
public abstract class AcervoItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biblioteca_id")
    private Biblioteca biblioteca;

    @Column(name = "tipo_item", nullable = false, insertable = false, updatable = false, length = 30)
    private String tipoItem;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusAcervo status = StatusAcervo.DISPONIVEL;

    @Column(name = "codigo_barras", length = 80)
    private String codigoBarras;

    @Column(name = "tombo", nullable = false, length = 80)
    private String tombo;

    @Column(name = "localizacao", length = 120)
    private String localizacao;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    public abstract TipoItem tipo();

    public void mudarStatus(StatusAcervo novoStatus) {
        this.status = this.status.transicionar(novoStatus);
        this.deletedAt = novoStatus == StatusAcervo.DESCARTADO ? LocalDateTime.now() : null;
    }

    public Biblioteca getBiblioteca() {
        return biblioteca;
    }

    public void setBiblioteca(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    public UUID getBibliotecaId() {
        return biblioteca != null ? biblioteca.getId() : null;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public StatusAcervo getStatus() {
        return status;
    }

    public void setStatus(StatusAcervo status) {
        this.status = status;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getTombo() {
        return tombo;
    }

    public void setTombo(String tombo) {
        this.tombo = tombo;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Long getVersao() {
        return versao;
    }
}
