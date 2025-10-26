package br.com.desafio.veiculos.infrastructure.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filtro de log que mascara informações sensíveis (PII - Personally Identifiable Information)
 * antes de serem escritas nos logs.
 */
public class PiiRedactionFilter extends Filter<ILoggingEvent> {

    // Padrões para identificar dados sensíveis
    private static final Pattern PLACA_PATTERN = Pattern.compile("\\b([A-Z]{3}[-]?\\d{4}|[A-Z]{3}\\d[A-Z]\\d{2})\\b");
    private static final Pattern RENAVAM_PATTERN = Pattern.compile("\\b(\\d{11})\\b");
    private static final Pattern VIN_PATTERN = Pattern.compile("\\b([A-HJ-NPR-Z0-9]{17})\\b");
    private static final Pattern CPF_PATTERN = Pattern.compile("\\b(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})\\b");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("\\b(\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}|\\d{14})\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4})\\b");

    @Override
    public FilterReply decide(ILoggingEvent event) {
        // Este filtro não bloqueia eventos, apenas os modifica
        // A modificação real acontece em outro componente
        return FilterReply.NEUTRAL;
    }

    /**
     * Mascara dados sensíveis em uma string
     */
    public static String redact(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String redacted = message;

        // Mascarar PLACA (ABC1234 -> A**1**4)
        Matcher placaMatcher = PLACA_PATTERN.matcher(redacted);
        while (placaMatcher.find()) {
            String placa = placaMatcher.group();
            String masked = maskPlaca(placa);
            redacted = redacted.replace(placa, masked);
        }

        // Mascarar RENAVAM (12345678901 -> 123****8901)
        Matcher renavamMatcher = RENAVAM_PATTERN.matcher(redacted);
        while (renavamMatcher.find()) {
            String renavam = renavamMatcher.group();
            String masked = maskMiddle(renavam, 3, 4);
            redacted = redacted.replace(renavam, masked);
        }

        // Mascarar VIN (1HGBH41JXMN109186 -> 1HG***********186)
        Matcher vinMatcher = VIN_PATTERN.matcher(redacted);
        while (vinMatcher.find()) {
            String vin = vinMatcher.group();
            String masked = maskMiddle(vin, 3, 3);
            redacted = redacted.replace(vin, masked);
        }

        // Mascarar CPF (123.456.789-01 -> ***.456.***-**)
        Matcher cpfMatcher = CPF_PATTERN.matcher(redacted);
        while (cpfMatcher.find()) {
            String cpf = cpfMatcher.group();
            redacted = redacted.replace(cpf, "***.***.***-**");
        }

        // Mascarar CNPJ
        Matcher cnpjMatcher = CNPJ_PATTERN.matcher(redacted);
        while (cnpjMatcher.find()) {
            String cnpj = cnpjMatcher.group();
            redacted = redacted.replace(cnpj, "**.***.***/****-**");
        }

        // Mascarar EMAIL (usuario@dominio.com -> u*****o@d****o.com)
        Matcher emailMatcher = EMAIL_PATTERN.matcher(redacted);
        while (emailMatcher.find()) {
            String email = emailMatcher.group();
            String masked = maskEmail(email);
            redacted = redacted.replace(email, masked);
        }

        // Mascarar TELEFONE
        Matcher phoneMatcher = PHONE_PATTERN.matcher(redacted);
        while (phoneMatcher.find()) {
            String phone = phoneMatcher.group();
            redacted = redacted.replace(phone, "(##) #####-####");
        }

        return redacted;
    }

    /**
     * Mascara uma placa mantendo primeiro, último e dígito do meio
     * ABC1234 -> A**1**4
     */
    private static String maskPlaca(String placa) {
        if (placa.length() < 7) {
            return "***";
        }
        String clean = placa.replaceAll("-", "");
        return clean.charAt(0) + "**" + clean.charAt(3) + "**" + clean.charAt(clean.length() - 1);
    }

    /**
     * Mascara o meio de uma string mantendo N caracteres no início e fim
     */
    private static String maskMiddle(String value, int keepStart, int keepEnd) {
        if (value.length() <= keepStart + keepEnd) {
            return "*".repeat(value.length());
        }
        String start = value.substring(0, keepStart);
        String end = value.substring(value.length() - keepEnd);
        int maskLength = value.length() - keepStart - keepEnd;
        return start + "*".repeat(maskLength) + end;
    }

    /**
     * Mascara email mantendo primeira e última letra do usuário e domínio
     * usuario@dominio.com -> u*****o@d****o.com
     */
    private static String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***@***.***";
        }

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
}

