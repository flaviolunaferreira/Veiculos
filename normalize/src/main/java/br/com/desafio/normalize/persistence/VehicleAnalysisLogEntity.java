package br.com.desafio.normalize.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicle_analysis_log")
@Getter
@Setter
@NoArgsConstructor
public class VehicleAnalysisLogEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String idInputType;

    @Column(nullable = false)
    private String idInputValue;

    @Column(nullable = false)
    private String vinCanonical;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String supplierCalls;

    @Column(nullable = false)
    private boolean hasConstraints;

    @Column(nullable = false)
    private long estimatedCostCents;

    @Column
    private String traceId;
}

