package br.dev.lourenco.scriba.modules.curadoria.domain;

import java.util.UUID;

import br.dev.lourenco.scriba.core.domain.TenantEntity;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "desbastamento")
public class Desbastamento extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acervo_item_id", nullable = false)
    private AcervoItem acervoItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoDesbastamento tipo;

    @Column(name = "justificativa", nullable = false, length = 1000)
    private String justificativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_biblioteca_id")
    private Biblioteca destinoBiblioteca;

    @Column(name = "destino_instituicao_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID destinoInstituicaoId;

    public AcervoItem getAcervoItem() {
        return acervoItem;
    }

    public void setAcervoItem(AcervoItem acervoItem) {
        this.acervoItem = acervoItem;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoDesbastamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDesbastamento tipo) {
        this.tipo = tipo;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public Biblioteca getDestinoBiblioteca() {
        return destinoBiblioteca;
    }

    public void setDestinoBiblioteca(Biblioteca destinoBiblioteca) {
        this.destinoBiblioteca = destinoBiblioteca;
    }

    public UUID getDestinoInstituicaoId() {
        return destinoInstituicaoId;
    }

    public void setDestinoInstituicaoId(UUID destinoInstituicaoId) {
        this.destinoInstituicaoId = destinoInstituicaoId;
    }
}
