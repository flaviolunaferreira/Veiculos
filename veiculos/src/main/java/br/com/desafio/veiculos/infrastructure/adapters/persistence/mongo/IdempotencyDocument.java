package br.com.desafio.veiculos.infrastructure.adapters.persistence.mongo;

import br.com.desafio.veiculos.domain.VehicleAnalysis;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "idempotency_store")
public class IdempotencyDocument {

    @Id
    private String id; // Idempotency-Key

    private VehicleAnalysis response;

    @Indexed(expireAfterSeconds = 86400) // 24 horas
    private Instant createdAt;

    public IdempotencyDocument(String id, VehicleAnalysis response) {
        this.id = id;
        this.response = response;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public VehicleAnalysis getResponse() {
        return response;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

