package br.com.desafio.veiculos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VehicleAnalysisTest {

    @Test
    void deveCriarAnaliseCompleta() {
        Constraints constraints = new Constraints(false, false);
        Infractions infractions = new Infractions(BigDecimal.ZERO, new ArrayList<>());
        Map<String, SupplierStatus> status = new HashMap<>();

        VehicleAnalysis analysis = new VehicleAnalysis(
            "9BWZZZ377VT004251",
            constraints,
            infractions,
            status
        );

        assertNotNull(analysis);
        assertEquals("9BWZZZ377VT004251", analysis.vin());
        assertNotNull(analysis.constraints());
        assertNotNull(analysis.infractions());
        assertNotNull(analysis.supplierStatus());
    }

    @Test
    void deveCriarAnaliseUsandoBuilder() {
        VehicleAnalysis analysis = VehicleAnalysis.builder()
            .vin("9BWZZZ377VT004251")
            .constraints(new Constraints(false, false))
            .infractions(new Infractions(BigDecimal.ZERO, new ArrayList<>()))
            .supplierStatus(new HashMap<>())
            .build();

        assertNotNull(analysis);
        assertEquals("9BWZZZ377VT004251", analysis.vin());
    }

    @Test
    void deveCriarAnaliseComRestricoes() {
        Constraints constraints = new Constraints(true, false);

        VehicleAnalysis analysis = VehicleAnalysis.builder()
            .vin("9BWHE41J484021768")
            .constraints(constraints)
            .build();

        assertNotNull(analysis.constraints());
        assertTrue(analysis.constraints().renajud());
        assertFalse(analysis.constraints().recall());
    }

    @Test
    void deveCriarAnaliseComInfracoes() {
        Infractions.InfractionDetail detail1 = new Infractions.InfractionDetail(
            "Excesso de velocidade",
            new BigDecimal("195.23")
        );
        Infractions.InfractionDetail detail2 = new Infractions.InfractionDetail(
            "Estacionamento irregular",
            new BigDecimal("130.00")
        );

        Infractions infractions = new Infractions(
            new BigDecimal("325.23"),
            java.util.List.of(detail1, detail2)
        );

        VehicleAnalysis analysis = VehicleAnalysis.builder()
            .vin("9BWZZZ377VT004251")
            .infractions(infractions)
            .build();

        assertNotNull(analysis.infractions());
        assertEquals(new BigDecimal("325.23"), analysis.infractions().totalAmount());
        assertEquals(2, analysis.infractions().details().size());
    }

    @Test
    void deveArmazenarStatusDosFornecedores() {
        Map<String, SupplierStatus> status = new HashMap<>();
        status.put("F1", new SupplierStatus(SupplierStatus.Status.SUCCESS, 150, null));
        status.put("F3", new SupplierStatus(SupplierStatus.Status.SUCCESS, 280, null));

        VehicleAnalysis analysis = VehicleAnalysis.builder()
            .vin("9BWZZZ377VT004251")
            .supplierStatus(status)
            .build();

        assertNotNull(analysis.supplierStatus());
        assertEquals(2, analysis.supplierStatus().size());
        assertTrue(analysis.supplierStatus().containsKey("F1"));
        assertTrue(analysis.supplierStatus().containsKey("F3"));
        assertEquals(SupplierStatus.Status.SUCCESS, analysis.supplierStatus().get("F1").status());
    }

    @Test
    void devePermitirCamposNulos() {
        VehicleAnalysis analysis = new VehicleAnalysis(null, null, null, null);

        assertNull(analysis.vin());
        assertNull(analysis.constraints());
        assertNull(analysis.infractions());
        assertNull(analysis.supplierStatus());
    }

    @Test
    void deveSerImutavel() {
        VehicleAnalysis analysis = VehicleAnalysis.builder()
            .vin("9BWZZZ377VT004251")
            .build();

        String vin = analysis.vin();
        assertEquals("9BWZZZ377VT004251", vin);
    }

    @Test
    void deveCriarAnaliseComRecall() {
        Constraints constraints = new Constraints(false, true);

        VehicleAnalysis analysis = VehicleAnalysis.builder()
            .vin("9BWDB45U5ET020689")
            .constraints(constraints)
            .build();

        assertNotNull(analysis.constraints());
        assertFalse(analysis.constraints().renajud());
        assertTrue(analysis.constraints().recall());
    }

    @Test
    void deveCriarAnaliseComAmbasRestricoes() {
        Constraints constraints = new Constraints(true, true);

        VehicleAnalysis analysis = VehicleAnalysis.builder()
            .vin("9BWAA45U59P098765")
            .constraints(constraints)
            .build();

        assertNotNull(analysis.constraints());
        assertTrue(analysis.constraints().renajud());
        assertTrue(analysis.constraints().recall());
    }
}

