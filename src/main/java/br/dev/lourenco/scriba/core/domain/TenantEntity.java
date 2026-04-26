package br.dev.lourenco.scriba.core.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "instituicao_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID instituicaoId;

    public UUID getInstituicaoId() {
        return instituicaoId;
    }

    public void setInstituicaoId(UUID instituicaoId) {
        this.instituicaoId = instituicaoId;
    }
}
