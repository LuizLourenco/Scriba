package br.dev.lourenco.scriba.modules.administracao.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class RegraEmprestimo {

    @Column(name = "prazo_padrao_dias", nullable = false)
    private int prazoPadraoDias = 14;

    @Column(name = "limite_emprestimos", nullable = false)
    private int limiteEmprestimos = 3;

    @Column(name = "valor_multa", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @Column(name = "limite_reservas", nullable = false)
    private int limiteReservas = 3;

    @Column(name = "dias_expiracao_reserva", nullable = false)
    private int diasExpiracaoReserva = 7;

    @Column(name = "maximo_renovacoes", nullable = false)
    private int maximoRenovacoes = 3;

    @Column(name = "bloqueio_com_multa", nullable = false)
    private boolean bloqueioComMulta = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_multa", nullable = false, length = 30)
    private TipoMulta tipoMulta = TipoMulta.FIXO_DIARIO;

    @Column(name = "teto_maximo_multa", precision = 10, scale = 2)
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
