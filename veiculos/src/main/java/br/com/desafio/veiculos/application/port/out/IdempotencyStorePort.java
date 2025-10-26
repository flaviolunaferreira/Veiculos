package br.com.desafio.veiculos.application.port.out;

import br.com.desafio.veiculos.domain.VehicleAnalysis;
import java.util.Optional;

public interface IdempotencyStorePort {
    Optional<VehicleAnalysis> getResponse(String idempotencyKey);
    void storeResponse(String idempotencyKey, VehicleAnalysis response);
}

