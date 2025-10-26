package br.com.desafio.veiculos.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyKeyGeneratorTest {

    private final IdempotencyKeyGenerator generator = new IdempotencyKeyGenerator();

    @Test
    void deveGerarChaveParaConteudoNaoNulo() {
        // Arrange
        String content = "{\"identifier\":{\"type\":\"PLACA\",\"value\":\"ABC1234\"}}";

        // Act
        String key = generator.generate(content);

        // Assert
        assertNotNull(key);
        assertFalse(key.isBlank());
        assertEquals(32, key.length());
    }

    @Test
    void deveGerarMesmaChaveParaMesmoConteudo() {
        // Arrange
        String content = "{\"identifier\":{\"type\":\"PLACA\",\"value\":\"ABC1234\"}}";

        // Act
        String key1 = generator.generate(content);
        String key2 = generator.generate(content);

        // Assert
        assertEquals(key1, key2, "Mesmo conteúdo deve gerar mesma chave");
    }

    @Test
    void deveGerarChavesDiferentesParaConteudosDiferentes() {
        // Arrange
        String content1 = "{\"identifier\":{\"type\":\"PLACA\",\"value\":\"ABC1234\"}}";
        String content2 = "{\"identifier\":{\"type\":\"PLACA\",\"value\":\"XYZ9876\"}}";

        // Act
        String key1 = generator.generate(content1);
        String key2 = generator.generate(content2);

        // Assert
        assertNotEquals(key1, key2, "Conteúdos diferentes devem gerar chaves diferentes");
    }

    @Test
    void deveGerarChaveParaConteudoVazio() {
        // Act
        String key = generator.generate("");

        // Assert
        assertNotNull(key);
        assertFalse(key.isBlank());
        // Conteúdo vazio gera UUID aleatório como fallback
    }

    @Test
    void deveGerarChaveParaConteudoNulo() {
        // Act
        String key = generator.generate((String) null);

        // Assert
        assertNotNull(key);
        assertFalse(key.isBlank());
        // Null gera UUID aleatório como fallback
    }

    @Test
    void deveGerarChaveComMultiplosParametros() {
        // Arrange
        String part1 = "PLACA";
        String part2 = "ABC1234";
        String part3 = "2025-10-26";

        // Act
        String key = generator.generate(part1, part2, part3);

        // Assert
        assertNotNull(key);
        assertEquals(32, key.length());
    }

    @Test
    void mesmaOrdemDeParametrosDevGerarMesmaChave() {
        // Act
        String key1 = generator.generate("A", "B", "C");
        String key2 = generator.generate("A", "B", "C");

        // Assert
        assertEquals(key1, key2);
    }

    @Test
    void ordemDiferenteDeParametrosDevGerarChaveDiferente() {
        // Act
        String key1 = generator.generate("A", "B", "C");
        String key2 = generator.generate("C", "B", "A");

        // Assert
        assertNotEquals(key1, key2, "Ordem diferente deve gerar chave diferente");
    }

    @Test
    void deveGerarChaveHexadecimal() {
        // Arrange
        String content = "{\"test\":\"data\"}";

        // Act
        String key = generator.generate(content);

        // Assert
        assertTrue(key.matches("[0-9a-f]{32}"), "Chave deve ser hexadecimal");
    }

    @Test
    void deveSerDeterministico() {
        // Arrange
        String content = "{\"identifier\":{\"type\":\"VIN\",\"value\":\"1HGBH41JXMN109186\"}}";

        // Act
        String key1 = generator.generate(content);
        // Aguardar um pouco
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        String key2 = generator.generate(content);

        // Assert
        assertEquals(key1, key2, "Deve ser determinístico independente do tempo");
    }
}

