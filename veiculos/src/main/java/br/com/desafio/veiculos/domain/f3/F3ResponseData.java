package br.com.desafio.veiculos.domain.f3;

import java.math.BigDecimal;
import java.util.List;

public record F3ResponseData(
    int totalInfractions,
    BigDecimal totalAmount,
    List<F3InfractionDetail> details
) {
    public record F3InfractionDetail(
        String description,
        BigDecimal amount
    ) {}
}

