package br.com.desafio.veiculos.infrastructure.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;

import java.nio.charset.StandardCharsets;

/**
 * Encoder customizado que aplica redação de PII antes de escrever o log
 */
public class PiiRedactionEncoder extends EncoderBase<ILoggingEvent> {

    private String pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        String originalMessage = event.getFormattedMessage();
        String redactedMessage = PiiRedactionFilter.redact(originalMessage);
        
        // Formata o log com a mensagem mascarada
        String formattedLog = String.format(
            "%s [%s] %-5s %s - %s%n",
            event.getTimeStamp(),
            event.getThreadName(),
            event.getLevel(),
            event.getLoggerName(),
            redactedMessage
        );
        
        return formattedLog.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}

