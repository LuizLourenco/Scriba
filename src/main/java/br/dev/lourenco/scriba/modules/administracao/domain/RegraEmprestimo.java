package br.dev.lourenco.scriba.modules.administracao.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RegraEmprestimo {

    @Column(name = "prazo_padrao_dias", nullable = false)
    private int prazoPadraoDias = 14;

    @Column(name = "limite_emprestimos", nullable = false)
    private int limiteEmprestimos = 3;

    @Column(name = "valor_multa", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    public int getPrazoPadraoDias() {
        return prazoPadraoDias;
    }

    public void setPrazoPadraoDias(int prazoPadraoDias) {
        this.prazoPadraoDias = prazoPadraoDias;
    }

    public int getLimiteEmprestimos() {
        return limiteEmprestimos;
    }

    public void setLimiteEmprestimos(int limiteEmprestimos) {
        this.limiteEmprestimos = limiteEmprestimos;
    }

    public BigDecimal getValorMulta() {
        return valorMulta;
    }

    public void setValorMulta(BigDecimal valorMulta) {
        this.valorMulta = valorMulta;
    }
}
