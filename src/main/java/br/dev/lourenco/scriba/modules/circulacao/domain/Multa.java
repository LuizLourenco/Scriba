package br.dev.lourenco.scriba.modules.circulacao.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.dev.lourenco.scriba.core.domain.TenantEntity;
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
@Table(name = "multa")
public class Multa extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emprestimo_id", nullable = false)
    private Emprestimo emprestimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "dias_atraso", nullable = false)
    private int diasAtraso;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusMulta status = StatusMulta.PENDENTE;

    @Column(name = "data_geracao", nullable = false)
    private LocalDate dataGeracao;

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public void setEmprestimo(Emprestimo emprestimo) {
        this.emprestimo = emprestimo;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }

    public StatusMulta getStatus() {
        return status;
    }

    public void setStatus(StatusMulta status) {
        this.status = status;
    }

    public LocalDate getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDate dataGeracao) {
        this.dataGeracao = dataGeracao;
    }
}
