package br.com.desafio.veiculos.domain.exception;

public class InfrastructureException extends VehicleAnalysisException {
    public InfrastructureException(String message) {
        super(message);
    }
    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}