package br.com.desafio.veiculos.application.usecase;

import br.com.desafio.veiculos.domain.VehicleAnalysis;

public interface VehicleAnalysisUseCase {
    VehicleAnalysis analyzeVehicle(String inputIdentifier, String idempotencyKey);
}

