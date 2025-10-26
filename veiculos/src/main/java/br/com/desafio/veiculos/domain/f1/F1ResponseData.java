package br.com.desafio.veiculos.domain.f1;

import br.com.desafio.veiculos.domain.Constraints;

// Esta classe seria gerada pelo JAX-B (wsimport) em um cenário SOAP real
public record F1ResponseData(
    String vin,
    Constraints restricoes
) {}

