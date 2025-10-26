package br.com.desafio.veiculos.infrastructure.adapters.soap;

import br.com.desafio.veiculos.application.port.out.SupplierPort;
import br.com.desafio.veiculos.domain.Constraints;
import br.com.desafio.veiculos.domain.SupplierResult;
import br.com.desafio.veiculos.domain.f1.F1ResponseData;
import br.com.desafio.veiculos.infrastructure.config.ResilienceConfiguration;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.util.concurrent.TimeoutException;

@Component("supplierF1Adapter")
public class SupplierF1Adapter implements SupplierPort {

    private static final Logger log = LoggerFactory.getLogger(SupplierF1Adapter.class);
    private static final String SUPPLIER_NAME = "F1";

    private final WebServiceTemplate webServiceTemplate;
    private final MeterRegistry meterRegistry;
    private final String f1Url;

    public SupplierF1Adapter(WebServiceTemplate webServiceTemplate, 
                             MeterRegistry meterRegistry,
                             @Value("${suppliers.f1.url}") String f1Url) {
        this.webServiceTemplate = webServiceTemplate;
        this.meterRegistry = meterRegistry;
        this.f1Url = f1Url;
    }

    @Override
    @RateLimiter(name = SUPPLIER_NAME, fallbackMethod = "fallbackRateLimiter")
    @CircuitBreaker(name = SUPPLIER_NAME, fallbackMethod = "fallback")
    @Retry(name = SUPPLIER_NAME, fallbackMethod = "fallback")
    @Bulkhead(name = SUPPLIER_NAME, fallbackMethod = "fallback")
    public SupplierResult<Object> fetchData(String vin) {
        Timer.Sample sample = Timer.start(meterRegistry);
        long start = System.nanoTime();
        
        try {
            // 1. Criar o request (JAX-B)
            Object request = createSoapRequest(vin);
            
            // 2. Enviar e receber (WebServiceTemplate)
            // Em produção, isso seria um objeto JAX-B complexo
            Object response = webServiceTemplate.marshalSendAndReceive(f1Url, request);
            
            // 3. Mapear a resposta (JAX-B -> Domain)
            F1ResponseData mappedResponse = mapSoapResponse(response, vin);
            
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            sample.stop(meterRegistry.timer("supplier.latency", "supplier", SUPPLIER_NAME, "status", "success"));
            return SupplierResult.success(SUPPLIER_NAME, latencyMs, mappedResponse);

        } catch (Exception e) {
            log.warn("Falha ao consultar F1 (SOAP) para o VIN: {}", vin, e);
            throw new RuntimeException(e);
        }
    }

    public SupplierResult<Object> fallback(String vin, TimeoutException e) {
        long latencyMs = ResilienceConfiguration.DEFAULT_TIMEOUT.toMillis();
        log.error("F1 (SOAP) fallback: Timeout para o VIN: {}", vin, e);
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(meterRegistry.timer("supplier.latency", "supplier", SUPPLIER_NAME, "status", "timeout"));
        return SupplierResult.timeout(SUPPLIER_NAME, latencyMs);
    }

    public SupplierResult<Object> fallbackRateLimiter(String vin, Throwable e) {
        log.warn("F1 (SOAP) fallback: Rate Limit excedido para o VIN: {}", vin, e);
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(meterRegistry.timer("supplier.latency", "supplier", SUPPLIER_NAME, "status", "failure"));
        return SupplierResult.failure(SUPPLIER_NAME, 0, "Rate limit exceeded");
    }

    public SupplierResult<Object> fallback(String vin, Throwable e) {
        log.error("F1 (SOAP) fallback: Erro geral para o VIN: {}", vin, e);
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(meterRegistry.timer("supplier.latency", "supplier", SUPPLIER_NAME, "status", "failure"));
        return SupplierResult.failure(SUPPLIER_NAME, 0, e.getMessage());
    }

    private Object createSoapRequest(String vin) {
        // STUB: Lógica de criação do objeto JAX-B
        // Ex: br.com.f1.GetVehicleDataRequest req = new br.com.f1.GetVehicleDataRequest();
        // req.setVin(vin);
        // return req;
        return new Object();
    }

    private F1ResponseData mapSoapResponse(Object response, String vin) {
        // STUB: Lógica de mapeamento JAX-B para F1ResponseData
        // Ex: br.com.f1.GetVehicleDataResponse res = (br.com.f1.GetVehicleDataResponse) response;
        // Constraints c = new Constraints(res.hasRenajud(), res.hasRecall());
        // return new F1ResponseData(vin, c);
        
        // Simulação para testes
        if ("VIN_COM_RESTRICAO".equals(vin)) {
            return new F1ResponseData(vin, new Constraints(true, true));
        }
        return new F1ResponseData(vin, new Constraints(false, false));
    }

    @Override
    public String getSupplierName() {
        return SUPPLIER_NAME;
    }
}

