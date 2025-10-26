package br.com.desafio.veiculos.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Status da consulta a um fornecedor específico")
public record SupplierStatus(
    @Schema(description = "Resultado da consulta (SUCCESS, FAILURE, TIMEOUT, NOT_CALLED)", example = "SUCCESS")
    Status status,
    @Schema(description = "Latência da chamada em milissegundos", example = "120")
    long latencyMs,
    @Schema(description = "Mensagem de erro, se houver falha", example = "Connection refused")
    String error
) {
    public enum Status {
        SUCCESS,
        FAILURE,
        TIMEOUT,
        NOT_CALLED
    }
}

