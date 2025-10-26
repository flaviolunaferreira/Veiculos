package br.com.desafio.veiculos.application.port.out;

import br.com.desafio.veiculos.domain.IdentifierType;

public interface IdentifierNormalizationPort {
    IdentifierType identifyType(String identifier);
    String normalizeToVin(String identifier, IdentifierType type);
}

