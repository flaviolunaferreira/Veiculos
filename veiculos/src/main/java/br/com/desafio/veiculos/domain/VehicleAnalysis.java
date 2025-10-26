package br.com.desafio.veiculos.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta consolidada da análise veicular")
public record VehicleAnalysis(
    @Schema(description = "VIN (Vehicle Identification Number) canônico", example = "9BWZZZ3T8D")
    String vin,

    @Schema(description = "Dados de restrições consolidadas")
    Constraints constraints,

    @Schema(description = "Dados de infrações consolidadas")
    Infractions infractions,

    @Schema(description = "Status detalhado da consulta a cada fornecedor (F1, F2, F3)")
    Map<String, SupplierStatus> supplierStatus
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String vin;
        private Constraints constraints;
        private Infractions infractions;
        private Map<String, SupplierStatus> supplierStatus;

        public Builder vin(String vin) {
            this.vin = vin;
            return this;
        }

        public Builder constraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public Builder infractions(Infractions infractions) {
            this.infractions = infractions;
            return this;
        }

        public Builder supplierStatus(Map<String, SupplierStatus> supplierStatus) {
            this.supplierStatus = supplierStatus;
            return this;
        }

        public VehicleAnalysis build() {
            return new VehicleAnalysis(vin, constraints, infractions, supplierStatus);
        }
    }
}

