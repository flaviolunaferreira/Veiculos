package br.com.desafio.veiculos.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Informações sobre infrações do veículo")
public record Infractions(
    @Schema(description = "Valor total das infrações pendentes", example = "450.75")
    BigDecimal totalAmount,
    @Schema(description = "Lista de infrações detalhadas")
    List<InfractionDetail> details
) {
    @Schema(description = "Detalhe de uma infração")
    public record InfractionDetail(
        @Schema(description = "Descrição da infração", example = "Excesso de velocidade")
        String description,
        @Schema(description = "Valor da infração", example = "195.23")
        BigDecimal amount
    ) {}
}

