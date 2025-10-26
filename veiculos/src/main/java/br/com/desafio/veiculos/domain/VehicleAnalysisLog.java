package br.com.desafio.veiculos.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record VehicleAnalysisLog(
    UUID id,
    Instant timestamp,
    IdentifierType idInputType,
    String idInputValue,
    String vinCanonical,
    Map<String, SupplierStatus> supplierCalls,
    boolean hasConstraints,
    long estimatedCostCents,
    String traceId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Instant timestamp;
        private IdentifierType idInputType;
        private String idInputValue;
        private String vinCanonical;
        private Map<String, SupplierStatus> supplierCalls;
        private boolean hasConstraints;
        private long estimatedCostCents;
        private String traceId;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder idInputType(IdentifierType idInputType) { this.idInputType = idInputType; return this; }
        public Builder idInputValue(String idInputValue) { this.idInputValue = idInputValue; return this; }
        public Builder vinCanonical(String vinCanonical) { this.vinCanonical = vinCanonical; return this; }
        public Builder supplierCalls(Map<String, SupplierStatus> supplierCalls) { this.supplierCalls = supplierCalls; return this; }
        public Builder hasConstraints(boolean hasConstraints) { this.hasConstraints = hasConstraints; return this; }
        public Builder estimatedCostCents(long estimatedCostCents) { this.estimatedCostCents = estimatedCostCents; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }

        public VehicleAnalysisLog build() {
            return new VehicleAnalysisLog(id, timestamp, idInputType, idInputValue, vinCanonical, supplierCalls, hasConstraints, estimatedCostCents, traceId);
        }
    }
}

