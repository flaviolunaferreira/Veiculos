package br.com.desafio.veiculos.infrastructure.config;

/**
 * Utilitário para mascarar dados sensíveis (PII) em logs e mensagens.
 * 
 * Uso:
 * <pre>
 * log.info("Consultando placa: {}", PiiRedactor.mask("ABC1234"));
 * log.debug("VIN recebido: {}", PiiRedactor.maskVin(vin));
 * </pre>
 */
public class PiiRedactor {

    private PiiRedactor() {
        // Classe utilitária - não deve ser instanciada
    }

    /**
     * Mascara qualquer dado sensível detectado automaticamente
     */
    public static String mask(String value) {
        return PiiRedactionFilter.redact(value);
    }

    /**
     * Mascara uma placa especificamente
     * ABC1234 -> A**1**4
     */
    public static String maskPlaca(String placa) {
        if (placa == null || placa.length() < 7) {
            return "***";
        }
        String clean = placa.replaceAll("-", "");
        return clean.charAt(0) + "**" + clean.charAt(3) + "**" + clean.charAt(clean.length() - 1);
    }

    /**
     * Mascara um VIN
     * 1HGBH41JXMN109186 -> 1HG***********186
     */
    public static String maskVin(String vin) {
        if (vin == null || vin.length() != 17) {
            return "***";
        }
        return vin.substring(0, 3) + "*".repeat(11) + vin.substring(14);
    }

    /**
     * Mascara um RENAVAM
     * 12345678901 -> 123****8901
     */
    public static String maskRenavam(String renavam) {
        if (renavam == null || renavam.length() != 11) {
            return "***";
        }
        return renavam.substring(0, 3) + "****" + renavam.substring(7);
    }

    /**
     * Mascara um CPF
     */
    public static String maskCpf(String cpf) {
        return "***.***.***-**";
    }

    /**
     * Mascara um CNPJ
     */
    public static String maskCnpj(String cnpj) {
        return "**.***.***/****-**";
    }

    /**
     * Mascara um email
     * usuario@dominio.com -> u*****o@d****o.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        String[] parts = email.split("@");
        String user = parts[0];
        String domain = parts[1];

        String maskedUser = user.length() > 2 
            ? user.charAt(0) + "*".repeat(user.length() - 2) + user.charAt(user.length() - 1)
            : "***";

        String maskedDomain = domain.length() > 2
            ? domain.charAt(0) + "*".repeat(domain.length() - 2) + domain.charAt(domain.length() - 1)
            : "***";

        return maskedUser + "@" + maskedDomain;
    }

    /**
     * Mascara um telefone
     */
    public static String maskPhone(String phone) {
        return "(##) #####-####";
    }
}

