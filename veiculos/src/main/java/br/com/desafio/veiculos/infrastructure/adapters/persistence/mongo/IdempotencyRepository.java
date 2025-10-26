package br.com.desafio.veiculos.infrastructure.adapters.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends MongoRepository<IdempotencyDocument, String> {
}

