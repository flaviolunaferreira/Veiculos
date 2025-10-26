package br.com.desafio.veiculos.infrastructure.adapters.normalization;

import br.com.desafio.veiculos.application.port.out.IdentifierNormalizationPort;
import br.com.desafio.veiculos.domain.IdentifierType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class IdentifierNormalizationAdapter implements IdentifierNormalizationPort {

    // Regex simples para exemplo
    private static final Pattern PLACA_PATTERN = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$"); // Mercosul/Antiga
    private static final Pattern RENAVAM_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    @Override
    public IdentifierType identifyType(String identifier) {
        if (identifier == null) return IdentifierType.INVALIDO;
        
        String upperId = identifier.toUpperCase().trim();
        
        if (VIN_PATTERN.matcher(upperId).matches()) {
            return IdentifierType.VIN;
        }
        if (PLACA_PATTERN.matcher(upperId).matches()) {
            return IdentifierType.PLACA;
        }
        if (RENAVAM_PATTERN.matcher(upperId).matches()) {
            return IdentifierType.RENAVAM;
        }
        return IdentifierType.INVALIDO;
    }

    @Override
    public String normalizeToVin(String identifier, IdentifierType type) {
        // Em um cenário real, PLACA e RENAVAM exigiriam uma chamada a um
        // serviço "Tabelas de-para" (ex: um DETRAN ou parceiro de dados).
        // Para este desafio, simulamos a conversão.
        
        String upperId = identifier.toUpperCase().trim();

        return switch (type) {
            case VIN -> upperId;
            case PLACA -> "VIN_DE_" + upperId; // STUB
            case RENAVAM -> "VIN_DE_" + upperId; // STUB
            case INVALIDO -> throw new IllegalArgumentException("Não é possível normalizar um identificador inválido");
        };
    }
}
