package br.com.desafio.veiculos.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações sobre restrições do veículo")
public record Constraints(
    @Schema(description = "Indica se há restrição RENAJUD ativa", example = "true")
    boolean renajud,
    @Schema(description = "Indica se há recall pendente", example = "false")
    boolean recall
) {}

