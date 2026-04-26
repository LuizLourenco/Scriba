package br.dev.lourenco.scriba.modules.administracao.dto;

import java.math.BigDecimal;

import br.dev.lourenco.scriba.modules.administracao.domain.TipoMulta;
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

    @Min(0)
    @Max(100)
    private int limiteReservas = 3;

    @Min(1)
    @Max(365)
    private int diasExpiracaoReserva = 7;

    @Min(0)
    @Max(100)
    private int maximoRenovacoes = 3;

    private boolean bloqueioComMulta = true;

    @NotNull
    private TipoMulta tipoMulta = TipoMulta.FIXO_DIARIO;

    @DecimalMin("0.00")
    private BigDecimal tetoMaximoMulta;

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

    public int getLimiteReservas() {
        return limiteReservas;
    }

    public void setLimiteReservas(int limiteReservas) {
        this.limiteReservas = limiteReservas;
    }

    public int getDiasExpiracaoReserva() {
        return diasExpiracaoReserva;
    }

    public void setDiasExpiracaoReserva(int diasExpiracaoReserva) {
        this.diasExpiracaoReserva = diasExpiracaoReserva;
    }

    public int getMaximoRenovacoes() {
        return maximoRenovacoes;
    }

    public void setMaximoRenovacoes(int maximoRenovacoes) {
        this.maximoRenovacoes = maximoRenovacoes;
    }

    public boolean isBloqueioComMulta() {
        return bloqueioComMulta;
    }

    public void setBloqueioComMulta(boolean bloqueioComMulta) {
        this.bloqueioComMulta = bloqueioComMulta;
    }

    public TipoMulta getTipoMulta() {
        return tipoMulta;
    }

    public void setTipoMulta(TipoMulta tipoMulta) {
        this.tipoMulta = tipoMulta;
    }

    public BigDecimal getTetoMaximoMulta() {
        return tetoMaximoMulta;
    }

    public void setTetoMaximoMulta(BigDecimal tetoMaximoMulta) {
        this.tetoMaximoMulta = tetoMaximoMulta;
    }
}
