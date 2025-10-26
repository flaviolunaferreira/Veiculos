package br.com.desafio.veiculos.api;

import br.com.desafio.veiculos.application.usecase.VehicleAnalysisUseCase;
import br.com.desafio.veiculos.domain.VehicleAnalysis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Tag(name = "Análise Veicular", description = "API para análise unificada de dados veiculares")
@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class VehicleAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(VehicleAnalysisController.class);

    private final VehicleAnalysisUseCase vehicleAnalysisUseCase;

    public VehicleAnalysisController(VehicleAnalysisUseCase vehicleAnalysisUseCase) {
        this.vehicleAnalysisUseCase = vehicleAnalysisUseCase;
    }

    @GetMapping("/{idveiculo}/analise")
    @Operation(
        summary = "Realiza análise unificada de dados veiculares",
        description = "Busca dados de múltiplas fontes (F1, F2, F3) a partir de um identificador (Placa, RENAVAM ou VIN) e consolida as informações. A idempotência é gerenciada automaticamente.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Análise veicular consolidada. Pode conter dados parciais em caso de falha de fornecedores.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleAnalysis.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Identificador inválido"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autorizado (Token JWT inválido ou ausente)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    public ResponseEntity<VehicleAnalysis> analyzeVehicle(
        @Parameter(description = "Identificador do veículo (Placa, RENAVAM ou VIN)", required = true, example = "ABC1234")
        @PathVariable String idveiculo
    ) {
        // Gera chave de idempotência automaticamente baseada no identificador
        // Usa hash SHA-256 para garantir unicidade e prevenir colisões
        String idempotencyKey = generateIdempotencyKey(idveiculo);

        MDC.put("idempotencyKey", idempotencyKey);
        log.info("Iniciando análise para o identificador: {} (idempotency: {})", idveiculo, idempotencyKey);

        VehicleAnalysis analysis = vehicleAnalysisUseCase.analyzeVehicle(idveiculo, idempotencyKey);

        log.info("Análise concluída para o identificador: {}", idveiculo);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Gera uma chave de idempotência baseada no identificador do veículo.
     * Utiliza SHA-256 para criar um hash único e determinístico.
     *
     * @param idveiculo Identificador do veículo (Placa, RENAVAM ou VIN)
     * @return Chave de idempotência (hash hexadecimal)
     */
    private String generateIdempotencyKey(String idveiculo) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(idveiculo.toUpperCase().trim().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // Fallback: usa o próprio identificador normalizado como chave
            log.warn("Erro ao gerar hash SHA-256, usando identificador como chave", e);
            return "idem_" + idveiculo.toUpperCase().trim().replaceAll("[^A-Z0-9]", "");
        }
    }
}

