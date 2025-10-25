package br.com.desafio.veiculos.domain.exception;

public abstract class VehicleAnalysisException extends RuntimeException {
    public VehicleAnalysisException(String message) {
        super(message);
    }
    public VehicleAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}