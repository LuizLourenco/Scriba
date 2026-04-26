package br.dev.lourenco.scriba.modules.administracao.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RegraEmprestimoForm {

    @Min(1)
    @Max(365)
    private int prazoPadraoDias = 14;

    @Min(1)
    @Max(100)
    private int limiteEmprestimos = 3;

    @NotNull
    @DecimalMin("0.00")
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
