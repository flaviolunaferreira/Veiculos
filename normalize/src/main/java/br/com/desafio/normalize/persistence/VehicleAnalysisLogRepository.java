package br.com.desafio.normalize.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface VehicleAnalysisLogRepository extends JpaRepository<VehicleAnalysisLogEntity, UUID> {
}

