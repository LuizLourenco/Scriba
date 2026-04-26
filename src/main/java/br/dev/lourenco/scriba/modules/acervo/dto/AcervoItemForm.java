package br.dev.lourenco.scriba.modules.acervo.dto;

import java.time.LocalDate;
import java.util.UUID;

import br.dev.lourenco.scriba.modules.acervo.domain.TipoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.TipoMidia;
import br.dev.lourenco.scriba.modules.acervo.domain.TipoPeriodico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AcervoItemForm {

    @NotNull
    private TipoItem tipoItem = TipoItem.LIVRO;

    @NotBlank
    @Size(max = 200)
    private String titulo;

    @NotBlank
    @Size(max = 80)
    private String tombo;

    @Size(max = 80)
    private String codigoBarras;

    @Size(max = 120)
    private String localizacao;

    private LocalDate dataAquisicao;

    private UUID bibliotecaId;

    private String isbn;
    private Integer numeroPaginas;
    private String edicao;
    private String volume;

    private TipoPeriodico tipoPeriodico = TipoPeriodico.REVISTA;
    private String issn;
    private String numero;

    private String remetente;
    private String destinatario;
    private LocalDate dataEnvio;

    private String assunto;
    private String fotografo;
    private String formato;
    private String resolucao;

    private TipoMidia tipoMidia = TipoMidia.CD;
    private String duracao;
    private String produtora;

    public TipoItem getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(TipoItem tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTombo() {
        return tombo;
    }

    public void setTombo(String tombo) {
        this.tombo = tombo;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
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

    public UUID getBibliotecaId() {
        return bibliotecaId;
    }

    public void setBibliotecaId(UUID bibliotecaId) {
        this.bibliotecaId = bibliotecaId;
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

    public TipoPeriodico getTipoPeriodico() {
        return tipoPeriodico;
    }

    public void setTipoPeriodico(TipoPeriodico tipoPeriodico) {
        this.tipoPeriodico = tipoPeriodico;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getFotografo() {
        return fotografo;
    }

    public void setFotografo(String fotografo) {
        this.fotografo = fotografo;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getResolucao() {
        return resolucao;
    }

    public void setResolucao(String resolucao) {
        this.resolucao = resolucao;
    }

    public TipoMidia getTipoMidia() {
        return tipoMidia;
    }

    public void setTipoMidia(TipoMidia tipoMidia) {
        this.tipoMidia = tipoMidia;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public String getProdutora() {
        return produtora;
    }

    public void setProdutora(String produtora) {
        this.produtora = produtora;
    }
}
