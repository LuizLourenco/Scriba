package br.dev.lourenco.scriba.modules.circulacao.domain;

import java.time.LocalDate;

import br.dev.lourenco.scriba.core.domain.TenantEntity;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "emprestimo")
public class Emprestimo extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acervo_item_id", nullable = false)
    private AcervoItem acervoItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "data_prevista_devolucao", nullable = false)
    private LocalDate dataPrevistaDevolucao;

    @Column(name = "data_efetiva_devolucao")
    private LocalDate dataEfetivaDevolucao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusEmprestimo status = StatusEmprestimo.ATIVO;

    @Column(name = "renovacoes", nullable = false)
    private int renovacoes;

    public boolean estaAtrasado(LocalDate hoje) {
        return status == StatusEmprestimo.ATIVO && dataPrevistaDevolucao.isBefore(hoje);
    }

    public void devolver(LocalDate dataDevolucao) {
        this.dataEfetivaDevolucao = dataDevolucao;
        this.status = StatusEmprestimo.DEVOLVIDO;
    }

    public void renovar(int dias) {
        this.dataPrevistaDevolucao = this.dataPrevistaDevolucao.plusDays(dias);
        this.renovacoes++;
    }

    public AcervoItem getAcervoItem() {
        return acervoItem;
    }

    public void setAcervoItem(AcervoItem acervoItem) {
        this.acervoItem = acervoItem;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) {
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }

    public LocalDate getDataEfetivaDevolucao() {
        return dataEfetivaDevolucao;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public void setStatus(StatusEmprestimo status) {
        this.status = status;
    }

    public int getRenovacoes() {
        return renovacoes;
    }

    public void setRenovacoes(int renovacoes) {
        this.renovacoes = renovacoes;
    }
}
