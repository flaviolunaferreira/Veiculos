package br.com.desafio.veiculos.application.service;

import br.com.desafio.veiculos.application.port.out.AnalysisLogPort;
import br.com.desafio.veiculos.application.port.out.IdentifierNormalizationPort;
import br.com.desafio.veiculos.application.port.out.IdempotencyStorePort;
import br.com.desafio.veiculos.application.port.out.SupplierPort;
import br.com.desafio.veiculos.application.usecase.VehicleAnalysisUseCase;
import br.com.desafio.veiculos.domain.IdentifierType;
import br.com.desafio.veiculos.domain.SupplierResult;
import br.com.desafio.veiculos.domain.VehicleAnalysis;
import br.com.desafio.veiculos.domain.VehicleAnalysisLog;
import br.com.desafio.veiculos.domain.f1.F1ResponseData;
import br.com.desafio.veiculos.infrastructure.mappers.VehicleAnalysisMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class VehicleAnalysisService implements VehicleAnalysisUseCase {

    private static final Logger log = LoggerFactory.getLogger(VehicleAnalysisService.class);

    private final IdentifierNormalizationPort identifierNormalizationPort;
    private final SupplierPort supplierF1Port;
    private final SupplierPort supplierF2Port;
    private final SupplierPort supplierF3Port;
    private final AnalysisLogPort analysisLogPort;
    private final IdempotencyStorePort idempotencyStore;
    private final VehicleAnalysisMapper mapper;
    private final ExecutorService analysisExecutor;
    private final MeterRegistry meterRegistry;

    public VehicleAnalysisService(
            IdentifierNormalizationPort identifierNormalizationPort,
            @Qualifier("supplierF1Adapter") SupplierPort supplierF1Port,
            @Qualifier("supplierF2Adapter") SupplierPort supplierF2Port,
            @Qualifier("supplierF3Adapter") SupplierPort supplierF3Port,
            AnalysisLogPort analysisLogPort,
            IdempotencyStorePort idempotencyStore,
            VehicleAnalysisMapper mapper,
            @Qualifier("analysisTaskExecutor") ExecutorService analysisExecutor,
            MeterRegistry meterRegistry) {
        this.identifierNormalizationPort = identifierNormalizationPort;
        this.supplierF1Port = supplierF1Port;
        this.supplierF2Port = supplierF2Port;
        this.supplierF3Port = supplierF3Port;
        this.analysisLogPort = analysisLogPort;
        this.idempotencyStore = idempotencyStore;
        this.mapper = mapper;
        this.analysisExecutor = analysisExecutor;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public VehicleAnalysis analyzeVehicle(String inputIdentifier, String idempotencyKey) {
        Instant startTime = Instant.now();

        Optional<VehicleAnalysis> cachedResponse = idempotencyStore.getResponse(idempotencyKey);
        if (cachedResponse.isPresent()) {
            log.warn("Requisição idempotente repetida detectada: {}", idempotencyKey);
            return cachedResponse.get();
        }

        IdentifierType inputType = identifierNormalizationPort.identifyType(inputIdentifier);
        if (inputType == IdentifierType.INVALIDO) {
            throw new IllegalArgumentException("Identificador inválido: " + inputIdentifier);
        }
        
        String vin = identifierNormalizationPort.normalizeToVin(inputIdentifier, inputType);
        MDC.put("vin", vin);

        log.info("VIN Canônico normalizado: {}", vin);

        Map<String, SupplierResult<?>> supplierResults = new HashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        CompletableFuture<SupplierResult<Object>> f1Future = CompletableFuture.supplyAsync(
                () -> supplierF1Port.fetchData(vin), analysisExecutor
        );

        CompletableFuture<SupplierResult<Object>> f3Future = CompletableFuture.supplyAsync(
                () -> supplierF3Port.fetchData(vin), analysisExecutor
        );

        CompletableFuture<SupplierResult<Object>> f2Future = f1Future.thenComposeAsync(f1Result -> {
            supplierResults.put("F1", f1Result);
            if (shouldCallF2(f1Result)) {
                log.info("Restrições F1 detectadas. Acionando F2.");
                return CompletableFuture.supplyAsync(() -> supplierF2Port.fetchData(vin), analysisExecutor);
            }
            return CompletableFuture.completedFuture(SupplierResult.notCalled("F2"));
        }, analysisExecutor);

        futures.add(f2Future.thenAccept(f2Result -> supplierResults.put("F2", f2Result)));
        futures.add(f3Future.thenAccept(f3Result -> supplierResults.put("F3", f3Result)));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        VehicleAnalysis consolidatedAnalysis = consolidateAnalysis(vin, supplierResults);

        logAnalysis(startTime, inputType, inputIdentifier, vin, consolidatedAnalysis);

        idempotencyStore.storeResponse(idempotencyKey, consolidatedAnalysis);
        
        Timer.builder("analysis.slo")
             .tag("f2_called", String.valueOf(supplierResults.get("F2").status().status() != br.com.desafio.veiculos.domain.SupplierStatus.Status.NOT_CALLED))
             .register(meterRegistry)
             .record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), java.util.concurrent.TimeUnit.MILLISECONDS);

        return consolidatedAnalysis;
    }

    private boolean shouldCallF2(SupplierResult<?> f1Result) {
        if (f1Result.data() instanceof F1ResponseData f1Data) {
            return f1Data.restricoes() != null && (f1Data.restricoes().renajud() || f1Data.restricoes().recall());
        }
        return false;
    }

    private VehicleAnalysis consolidateAnalysis(String vin, Map<String, SupplierResult<?>> supplierResults) {
        VehicleAnalysis.Builder builder = VehicleAnalysis.builder().vin(vin);
        Map<String, br.com.desafio.veiculos.domain.SupplierStatus> statuses = new HashMap<>();

        supplierResults.forEach((supplierName, result) -> {
            statuses.put(supplierName, result.status());
            if (result.status().status() == br.com.desafio.veiculos.domain.SupplierStatus.Status.SUCCESS && result.data() != null) {
                mapper.mergeAnalysisData(builder, result.data());
            }
        });

        return builder.supplierStatus(statuses).build();
    }

    private void logAnalysis(Instant startTime, IdentifierType inputType, String inputIdentifier, String vin, VehicleAnalysis analysis) {
        try {
            long costCents = calculateCost(analysis.supplierStatus());

            VehicleAnalysisLog logEntry = VehicleAnalysisLog.builder()
                    .id(UUID.randomUUID())
                    .timestamp(startTime)
                    .idInputType(inputType)
                    .idInputValue(inputIdentifier)
                    .vinCanonical(vin)
                    .supplierCalls(analysis.supplierStatus())
                    .hasConstraints(Optional.ofNullable(analysis.constraints()).map(c -> c.renajud() || c.recall()).orElse(false))
                    .estimatedCostCents(costCents)
                    .traceId(MDC.get("traceId"))
                    .build();

            analysisLogPort.logAnalysis(logEntry);

            meterRegistry.counter("analysis.cost.cents", "total", String.valueOf(costCents)).increment();

        } catch (Exception e) {
            log.error("Falha ao enviar log da análise para o Kafka", e);
        }
    }
    
    private long calculateCost(Map<String, br.com.desafio.veiculos.domain.SupplierStatus> statuses) {
        long cost = 0;
        if(statuses.getOrDefault("F1", SupplierResult.notCalled("F1").status()).status() == br.com.desafio.veiculos.domain.SupplierStatus.Status.SUCCESS) cost += 10;
        if(statuses.getOrDefault("F2", SupplierResult.notCalled("F2").status()).status() == br.com.desafio.veiculos.domain.SupplierStatus.Status.SUCCESS) cost += 25;
        if(statuses.getOrDefault("F3", SupplierResult.notCalled("F3").status()).status() == br.com.desafio.veiculos.domain.SupplierStatus.Status.SUCCESS) cost += 15;
        return cost;
    }
}

