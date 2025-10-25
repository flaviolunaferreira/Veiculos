package br.com.desafio.veiculos.domain.exception;

public class DomainException extends VehicleAnalysisException {
    public DomainException(String message) {
        super(message);
    }
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}