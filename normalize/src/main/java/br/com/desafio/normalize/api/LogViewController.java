package br.com.desafio.normalize.api;

import br.com.desafio.normalize.persistence.VehicleAnalysisLogEntity;
import br.com.desafio.normalize.persistence.VehicleAnalysisLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogViewController {

    private final VehicleAnalysisLogRepository repository;

    @GetMapping
    public ResponseEntity<Page<VehicleAnalysisLogEntity>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<VehicleAnalysisLogEntity> logs = repository.findAll(pageRequest);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleAnalysisLogEntity> getLogById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", repository.count());
        stats.put("timestamp", java.time.Instant.now());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/latest")
    public ResponseEntity<VehicleAnalysisLogEntity> getLatest() {
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("timestamp").descending());
        Page<VehicleAnalysisLogEntity> page = repository.findAll(pageRequest);
        
        if (page.hasContent()) {
            return ResponseEntity.ok(page.getContent().get(0));
        }
        return ResponseEntity.notFound().build();
    }
}

