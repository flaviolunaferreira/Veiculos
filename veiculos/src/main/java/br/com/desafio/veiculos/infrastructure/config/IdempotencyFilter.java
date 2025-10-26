package br.com.desafio.veiculos.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro que intercepta requisições e garante que todas tenham Idempotency-Key.
 * 
 * Comportamento:
 * 1. Se cliente enviar Idempotency-Key → usa a enviada
 * 2. Se não enviar → gera automaticamente baseado no body
 * 3. Adiciona o header na requisição para uso downstream
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    
    private final IdempotencyKeyGenerator keyGenerator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        // Verifica se já tem Idempotency-Key
        String existingKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if (existingKey != null && !existingKey.isBlank()) {
            log.debug("Idempotency-Key fornecida pelo cliente: {}", existingKey);
            filterChain.doFilter(request, response);
            return;
        }

        // Precisa ler o body para gerar chave
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String body = cachedRequest.getCachedBody();

        // Gera chave automaticamente
        String generatedKey = keyGenerator.generate(body);
        log.info("Idempotency-Key gerada automaticamente: {}", generatedKey);

        // Cria wrapper com o header adicional
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(cachedRequest) {
            private final Map<String, String> customHeaders = new HashMap<>();

            {
                customHeaders.put(IDEMPOTENCY_KEY_HEADER, generatedKey);
            }

            @Override
            public String getHeader(String name) {
                String headerValue = customHeaders.get(name);
                if (headerValue != null) {
                    return headerValue;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Map<String, String> allHeaders = new HashMap<>();
                Enumeration<String> originalHeaders = super.getHeaderNames();
                while (originalHeaders.hasMoreElements()) {
                    String name = originalHeaders.nextElement();
                    allHeaders.put(name, super.getHeader(name));
                }
                allHeaders.putAll(customHeaders);
                return Collections.enumeration(allHeaders.keySet());
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (customHeaders.containsKey(name)) {
                    return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
                }
                return super.getHeaders(name);
            }
        };

        filterChain.doFilter(wrappedRequest, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Não aplica filtro em endpoints de health, actuator, etc
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs");
    }

    /**
     * Wrapper que cacheia o body da requisição para múltiplas leituras
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        }

        public String getCachedBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
        }

        @Override
        public jakarta.servlet.ServletInputStream getInputStream() {
            return new jakarta.servlet.ServletInputStream() {
                private final ByteArrayInputStream buffer = new ByteArrayInputStream(cachedBody);

                @Override
                public int read() {
                    return buffer.read();
                }

                @Override
                public boolean isFinished() {
                    return buffer.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(jakarta.servlet.ReadListener listener) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}

