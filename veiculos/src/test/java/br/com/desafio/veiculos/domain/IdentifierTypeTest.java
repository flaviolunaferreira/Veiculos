package br.com.desafio.veiculos.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentifierTypeTest {

    @Test
    void deveConterTipoPlaca() {
        assertNotNull(IdentifierType.PLACA);
        assertEquals("PLACA", IdentifierType.PLACA.name());
    }

    @Test
    void deveConterTipoRenavam() {
        assertNotNull(IdentifierType.RENAVAM);
        assertEquals("RENAVAM", IdentifierType.RENAVAM.name());
    }

    @Test
    void deveConterTipoVin() {
        assertNotNull(IdentifierType.VIN);
        assertEquals("VIN", IdentifierType.VIN.name());
    }

    @Test
    void deveConterTipoInvalido() {
        assertNotNull(IdentifierType.INVALIDO);
        assertEquals("INVALIDO", IdentifierType.INVALIDO.name());
    }

    @Test
    void deveTerQuatroTipos() {
        IdentifierType[] types = IdentifierType.values();
        assertEquals(4, types.length);
    }

    @Test
    void deveConverterDeString() {
        assertEquals(IdentifierType.PLACA, IdentifierType.valueOf("PLACA"));
        assertEquals(IdentifierType.RENAVAM, IdentifierType.valueOf("RENAVAM"));
        assertEquals(IdentifierType.VIN, IdentifierType.valueOf("VIN"));
        assertEquals(IdentifierType.INVALIDO, IdentifierType.valueOf("INVALIDO"));
    }

    @Test
    void deveLancarExcecaoParaTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            IdentifierType.valueOf("INEXISTENTE");
        });
    }
}

