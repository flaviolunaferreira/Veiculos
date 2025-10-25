package br.com.desafio.veiculos.domain.exception;

public class NormalizationException extends InfrastructureException {
    public NormalizationException(String message) {
        super(message);
    }
    public NormalizationException(String message, Throwable cause) {
        super(message, cause);
    }
}