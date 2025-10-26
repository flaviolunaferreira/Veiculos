package br.com.desafio.normalize.consumer;

import br.com.desafio.normalize.domain.VehicleAnalysisLog;
import br.com.desafio.normalize.persistence.VehicleAnalysisLogEntity;
import br.com.desafio.normalize.persistence.VehicleAnalysisLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisLogConsumer {

    private final VehicleAnalysisLogRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "vehicle_analysis_log", groupId = "analysis-log-persister")
    public void consume(@Payload VehicleAnalysisLog logMessage) {
        if (logMessage == null) {
            log.warn("Recebida mensagem nula do Kafka.");
            return;
        }

        try {
            log.info("Recebido log de análise do Kafka. TraceId: {}", logMessage.getTraceId());

            VehicleAnalysisLogEntity entity = new VehicleAnalysisLogEntity();
            entity.setId(logMessage.getId());
            entity.setTimestamp(logMessage.getTimestamp());
            entity.setIdInputType(logMessage.getIdInputType());
            entity.setIdInputValue(logMessage.getIdInputValue());
            entity.setVinCanonical(logMessage.getVinCanonical());
            entity.setHasConstraints(logMessage.isHasConstraints());
            entity.setEstimatedCostCents(logMessage.getEstimatedCostCents());
            entity.setTraceId(logMessage.getTraceId());

            String supplierCallsJson = objectMapper.writeValueAsString(logMessage.getSupplierCalls());
            entity.setSupplierCalls(supplierCallsJson);

            repository.save(entity);
            log.info("Log de análise persistido no banco de dados. ID: {}", entity.getId());

        } catch (Exception e) {
            log.error("Erro ao persistir log de análise do Kafka. TraceId: {}. Mensagem: {}", 
                logMessage.getTraceId(), e.getMessage(), e);
        }
    }
}

