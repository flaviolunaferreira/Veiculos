package br.com.desafio.veiculos.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PiiRedactorTest {

    @Test
    void deveMascararPlaca() {
        // Arrange
        String placa = "ABC1234";
        
        // Act
        String masked = PiiRedactor.maskPlaca(placa);
        
        // Assert
        assertEquals("A**1**4", masked);
        assertFalse(masked.contains("ABC"));
        assertFalse(masked.contains("234"));
    }

    @Test
    void deveMascararPlacaMercosul() {
        // Arrange
        String placa = "ABC1D23";
        
        // Act
        String masked = PiiRedactor.maskPlaca(placa);
        
        // Assert
        assertEquals("A**1**3", masked);
    }

    @Test
    void deveMascararVin() {
        // Arrange
        String vin = "1HGBH41JXMN109186";
        
        // Act
        String masked = PiiRedactor.maskVin(vin);
        
        // Assert
        assertEquals("1HG***********186", masked);
        assertTrue(masked.startsWith("1HG"));
        assertTrue(masked.endsWith("186"));
        assertEquals(17, masked.length());
    }

    @Test
    void deveMascararRenavam() {
        // Arrange
        String renavam = "12345678901";
        
        // Act
        String masked = PiiRedactor.maskRenavam(renavam);
        
        // Assert
        assertEquals("123****8901", masked);
        assertTrue(masked.startsWith("123"));
        assertTrue(masked.endsWith("8901"));
    }

    @Test
    void deveMascararEmail() {
        // Arrange
        String email = "usuario@dominio.com";
        
        // Act
        String masked = PiiRedactor.maskEmail(email);
        
        // Assert
        assertTrue(masked.startsWith("u"));
        assertTrue(masked.endsWith("m"));
        assertTrue(masked.contains("@"));
        assertFalse(masked.contains("usuario"));
        assertFalse(masked.contains("dominio"));
    }

    @Test
    void deveMascararCpf() {
        // Arrange
        String cpf = "123.456.789-01";
        
        // Act
        String masked = PiiRedactor.maskCpf(cpf);
        
        // Assert
        assertEquals("***.***.***-**", masked);
    }

    @Test
    void deveMascararTextoComMultiplosDados() {
        // Arrange
        String texto = "Consultando veículo placa ABC1234 VIN 1HGBH41JXMN109186 RENAVAM 12345678901";
        
        // Act
        String masked = PiiRedactor.mask(texto);
        
        // Assert
        assertFalse(masked.contains("ABC1234"));
        assertFalse(masked.contains("1HGBH41JXMN109186"));
        assertFalse(masked.contains("12345678901"));
        assertTrue(masked.contains("A**1**4") || masked.contains("***"));
    }

    @Test
    void deveTratarValoresNulos() {
        // Act & Assert
        assertDoesNotThrow(() -> PiiRedactor.mask(null));
        assertDoesNotThrow(() -> PiiRedactor.maskPlaca(null));
        assertDoesNotThrow(() -> PiiRedactor.maskVin(null));
        assertDoesNotThrow(() -> PiiRedactor.maskEmail(null));
    }

    @Test
    void deveTratarValoresVazios() {
        // Act & Assert
        assertEquals("", PiiRedactor.mask(""));
        assertEquals("***", PiiRedactor.maskPlaca(""));
        assertEquals("***", PiiRedactor.maskVin(""));
    }

    @Test
    void deveMascararTelefone() {
        // Arrange
        String telefone = "(11) 98765-4321";
        
        // Act
        String masked = PiiRedactor.maskPhone(telefone);
        
        // Assert
        assertEquals("(##) #####-####", masked);
        assertFalse(masked.contains("98765"));
    }
}

