package br.com.desafio.veiculos.infrastructure.adapters.persistence.mongo;

import br.com.desafio.veiculos.application.port.out.IdempotencyStorePort;
import br.com.desafio.veiculos.domain.VehicleAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdempotencyStoreMongoAdapter implements IdempotencyStorePort {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyStoreMongoAdapter.class);
    
    private final IdempotencyRepository repository;

    public IdempotencyStoreMongoAdapter(IdempotencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<VehicleAnalysis> getResponse(String idempotencyKey) {
        try {
            return repository.findById(idempotencyKey)
                             .map(IdempotencyDocument::getResponse);
        } catch (Exception e) {
            log.error("Erro ao buscar chave de idempotência no MongoDB: {}", idempotencyKey, e);
            return Optional.empty();
        }
    }

    @Override
    public void storeResponse(String idempotencyKey, VehicleAnalysis response) {
        try {
            IdempotencyDocument document = new IdempotencyDocument(idempotencyKey, response);
            repository.save(document);
        } catch (Exception e) {
            log.error("Erro ao salvar resposta de idempotência no MongoDB: {}", idempotencyKey, e);
        }
    }
}

