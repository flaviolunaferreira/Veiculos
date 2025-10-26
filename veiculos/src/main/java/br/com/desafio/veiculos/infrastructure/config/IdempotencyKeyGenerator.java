package br.com.desafio.veiculos.infrastructure.config;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Gerador de chaves de idempotência automáticas.
 * 
 * Estratégia:
 * 1. Se cliente não enviar Idempotency-Key, geramos automaticamente
 * 2. Geramos com base no hash do conteúdo da requisição
 * 3. Garantimos que mesma requisição = mesma chave
 */
@Component
public class IdempotencyKeyGenerator {

    /**
     * Gera chave de idempotência baseada no conteúdo da requisição
     * 
     * @param content Conteúdo da requisição (JSON serializado)
     * @return Chave única para esse conteúdo
     */
    public String generate(String content) {
        if (content == null || content.isBlank()) {
            // Fallback: UUID aleatório
            return UUID.randomUUID().toString();
        }

        try {
            // Gera hash SHA-256 do conteúdo
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            
            // Converte para string hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Retorna primeiros 32 caracteres (suficiente para unicidade)
            return hexString.substring(0, 32);
            
        } catch (NoSuchAlgorithmException e) {
            // Fallback: UUID aleatório
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Gera chave de idempotência baseada em múltiplos parâmetros
     */
    public String generate(String... parts) {
        String combined = String.join("|", parts);
        return generate(combined);
    }
}

