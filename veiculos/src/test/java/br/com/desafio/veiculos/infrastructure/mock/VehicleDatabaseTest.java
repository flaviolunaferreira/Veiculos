package br.com.desafio.veiculos.infrastructure.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VehicleDatabaseTest {

    private VehicleDatabase vehicleDatabase;

    @BeforeEach
    void setUp() {
        vehicleDatabase = new VehicleDatabase();
    }

    @Test
    void deveBuscarVeiculoPorPlaca() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("ABC1234");

        assertTrue(result.isPresent());
        assertEquals("9BWZZZ377VT004251", result.get().getVin());
        assertEquals("Volkswagen", result.get().getMarca());
        assertEquals("Gol", result.get().getModelo());
    }

    @Test
    void deveBuscarVeiculoPorRenavam() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("12345678901");

        assertTrue(result.isPresent());
        assertEquals("9BWZZZ377VT004251", result.get().getVin());
        assertEquals("ABC1234", result.get().getPlaca());
    }

    @Test
    void deveBuscarVeiculoPorVin() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("9BWZZZ377VT004251");

        assertTrue(result.isPresent());
        assertEquals("ABC1234", result.get().getPlaca());
        assertEquals("12345678901", result.get().getRenavam());
    }

    @Test
    void mesmoVeiculoDeveSerRetornadoPorQualquerIdentificador() {
        Optional<VehicleDatabase.VehicleData> byPlaca = vehicleDatabase.findByIdentifier("ABC1234");
        Optional<VehicleDatabase.VehicleData> byRenavam = vehicleDatabase.findByIdentifier("12345678901");
        Optional<VehicleDatabase.VehicleData> byVin = vehicleDatabase.findByIdentifier("9BWZZZ377VT004251");

        assertTrue(byPlaca.isPresent());
        assertTrue(byRenavam.isPresent());
        assertTrue(byVin.isPresent());

        assertEquals(byPlaca.get().getVin(), byRenavam.get().getVin());
        assertEquals(byPlaca.get().getVin(), byVin.get().getVin());
    }

    @Test
    void deveBuscarVeiculoComRenajud() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("MNO1234");

        assertTrue(result.isPresent());
        assertTrue(result.get().isRenajud());
        assertFalse(result.get().isRecall());
    }

    @Test
    void deveBuscarVeiculoComRecall() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("VWX3456");

        assertTrue(result.isPresent());
        assertFalse(result.get().isRenajud());
        assertTrue(result.get().isRecall());
    }

    @Test
    void deveBuscarVeiculoComAmbasRestricoes() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("EFG5678");

        assertTrue(result.isPresent());
        assertTrue(result.get().isRenajud());
        assertTrue(result.get().isRecall());
    }

    @Test
    void deveRetornarVazioParaIdentificadorInexistente() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("ZZZ9999");

        assertFalse(result.isPresent());
    }

    @Test
    void deveRetornarVazioParaIdentificadorNulo() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier(null);

        assertFalse(result.isPresent());
    }

    @Test
    void deveRetornarVazioParaIdentificadorVazio() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("");

        assertFalse(result.isPresent());
    }

    @Test
    void deveBuscarVeiculoImportado() {
        Optional<VehicleDatabase.VehicleData> result = vehicleDatabase.findByIdentifier("KLM3456");

        assertTrue(result.isPresent());
        assertEquals("Honda", result.get().getMarca());
        assertEquals("Civic", result.get().getModelo());
        assertEquals("1HGCV1F30JA123456", result.get().getVin());
    }

    @Test
    void deveRetornarEstatisticas() {
        VehicleDatabase.DatabaseStats stats = vehicleDatabase.getStats();

        assertNotNull(stats);
        assertTrue(stats.getTotalVehicles() > 30);
        assertTrue(stats.getWithRenajud() >= 3);
        assertTrue(stats.getWithRecall() >= 3);
    }

    @Test
    void deveBuscarVeiculoCaseInsensitive() {
        Optional<VehicleDatabase.VehicleData> lower = vehicleDatabase.findByIdentifier("abc1234");
        Optional<VehicleDatabase.VehicleData> upper = vehicleDatabase.findByIdentifier("ABC1234");
        Optional<VehicleDatabase.VehicleData> mixed = vehicleDatabase.findByIdentifier("AbC1234");

        assertTrue(lower.isPresent());
        assertTrue(upper.isPresent());
        assertTrue(mixed.isPresent());

        assertEquals(lower.get().getVin(), upper.get().getVin());
        assertEquals(lower.get().getVin(), mixed.get().getVin());
    }
}

