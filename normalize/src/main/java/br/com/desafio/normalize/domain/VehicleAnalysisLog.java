package br.com.desafio.normalize.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleAnalysisLog {
    private UUID id;
    private Instant timestamp;
    private String idInputType;
    private String idInputValue;
    private String vinCanonical;
    private Map<String, SupplierStatus> supplierCalls;
    private boolean hasConstraints;
    private long estimatedCostCents;
    private String traceId;
}

