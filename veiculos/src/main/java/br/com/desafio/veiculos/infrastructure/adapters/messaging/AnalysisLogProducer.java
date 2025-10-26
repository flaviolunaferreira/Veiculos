package br.com.desafio.veiculos.infrastructure.adapters.messaging;

import br.com.desafio.veiculos.application.port.out.AnalysisLogPort;
import br.com.desafio.veiculos.domain.VehicleAnalysisLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnalysisLogProducer implements AnalysisLogPort {

    private static final Logger log = LoggerFactory.getLogger(AnalysisLogProducer.class);
    private static final String TOPIC = "vehicle_analysis_log";

    private final KafkaTemplate<String, VehicleAnalysisLog> kafkaTemplate;

    public AnalysisLogProducer(KafkaTemplate<String, VehicleAnalysisLog> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void logAnalysis(VehicleAnalysisLog log) {
        try {
            kafkaTemplate.send(TOPIC, log.vinCanonical(), log);
            AnalysisLogProducer.log.debug("Log de análise enviado para Kafka: VIN={}", log.vinCanonical());
        } catch (Exception e) {
            AnalysisLogProducer.log.error("Erro ao enviar log para Kafka: {}", log.vinCanonical(), e);
        }
    }
}
