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
@Table(name = "reserva")
public class Reserva extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acervo_item_id", nullable = false)
    private AcervoItem acervoItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusReserva status = StatusReserva.AGUARDANDO;

    @Column(name = "data_reserva", nullable = false)
    private LocalDate dataReserva;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDate dataExpiracao;

    @Column(name = "posicao_fila", nullable = false)
    private int posicaoFila;

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

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    public LocalDate getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDate dataReserva) {
        this.dataReserva = dataReserva;
    }

    public LocalDate getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDate dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public int getPosicaoFila() {
        return posicaoFila;
    }

    public void setPosicaoFila(int posicaoFila) {
        this.posicaoFila = posicaoFila;
    }
}
