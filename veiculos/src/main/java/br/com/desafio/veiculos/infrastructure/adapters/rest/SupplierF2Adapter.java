package br.com.desafio.veiculos.infrastructure.adapters.rest;

import br.com.desafio.veiculos.application.port.out.SupplierPort;
import br.com.desafio.veiculos.domain.SupplierResult;
import br.com.desafio.veiculos.domain.f2.F2ResponseData;
import br.com.desafio.veiculos.infrastructure.adapters.rest.client.SupplierF2Client;
import br.com.desafio.veiculos.infrastructure.config.ResilienceConfiguration;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component("supplierF2Adapter")
public class SupplierF2Adapter implements SupplierPort {

    private static final Logger log = LoggerFactory.getLogger(SupplierF2Adapter.class);
    private static final String SUPPLIER_NAME = "F2";

    private final SupplierF2Client f2Client;
    private final MeterRegistry meterRegistry;

    public SupplierF2Adapter(SupplierF2Client f2Client, MeterRegistry meterRegistry) {
        this.f2Client = f2Client;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @CircuitBreaker(name = SUPPLIER_NAME, fallbackMethod = "fallback")
    @Retry(name = SUPPLIER_NAME, fallbackMethod = "fallback")
    @Bulkhead(name = SUPPLIER_NAME, fallbackMethod = "fallback")
    public SupplierResult<Object> fetchData(String vin) {
        Timer.Sample sample = Timer.start(meterRegistry);
        long start = System.nanoTime();
        
        try {
            F2ResponseData response = f2Client.getVehicleData(vin);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            
            sample.stop(meterRegistry.timer("supplier.latency", "supplier", SUPPLIER_NAME, "status", "success"));
            return SupplierResult.success(SUPPLIER_NAME, latencyMs, response);

        } catch (Exception e) {
            log.warn("Falha ao consultar F2 para o VIN: {}", vin, e);
            throw new RuntimeException(e);
        }
    }

    public SupplierResult<Object> fallback(String vin, TimeoutException e) {
        long latencyMs = ResilienceConfiguration.F2_TIMEOUT.toMillis();
        log.error("F2 (REST) fallback: Timeout para o VIN: {}", vin, e);
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(meterRegistry.timer("supplier.latency", "supplier", SUPPLIER_NAME, "status", "timeout"));
        return SupplierResult.timeout(SUPPLIER_NAME, latencyMs);
    }
    
    public SupplierResult<Object> fallback(String vin, Throwable e) {
        log.error("F2 (REST) fallback: Erro geral para o VIN: {}", vin, e);
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(meterRegistry.timer("supplier.latency", "supplier", SUPPLIER_NAME, "status", "failure"));
        return SupplierResult.failure(SUPPLIER_NAME, 0, e.getMessage());
    }

    @Override
    public String getSupplierName() {
        return SUPPLIER_NAME;
    }
}

