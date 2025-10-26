package br.com.desafio.veiculos.application.port.out;

import br.com.desafio.veiculos.domain.VehicleAnalysisLog;

public interface AnalysisLogPort {
    void logAnalysis(VehicleAnalysisLog logEntry);
}

