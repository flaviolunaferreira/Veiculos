package br.com.desafio.veiculos.infrastructure.mock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Base de dados simulada de veículos para fins de demonstração.
 * Em produção, F1 e F3 teriam acesso à base real do DENATRAN ou APIs especializadas.
 */
@Slf4j
@Component
public class VehicleDatabase {

    private static final Map<String, VehicleData> DATABASE = new HashMap<>();

    static {
        log.info("Inicializando base de dados mock com veículos...");

        // Veículos Nacionais Populares
        add("ABC1234", "12345678901", "9BWZZZ377VT004251", "Volkswagen", "Gol", 2020, false, false);
        add("XYZ5678", "23456789012", "9BWCA05U8EP047326", "Volkswagen", "Polo", 2019, false, false);
        add("DEF9012", "34567890123", "8AFZZZ54ABJ278405", "Fiat", "Uno", 2021, false, false);
        add("GHI3456", "45678901234", "9BD147068E8001122", "Chevrolet", "Onix", 2022, false, false);
        add("JKL7890", "56789012345", "93Y2SRD78HJ212345", "Toyota", "Corolla", 2021, false, false);
        
        // Veículos com Restrições (RENAJUD)
        add("MNO1234", "67890123456", "9BWHE41J484021768", "Volkswagen", "Fox", 2018, true, false);
        add("PQR5678", "78901234567", "93Y3SRFK1GJ180567", "Toyota", "Hilux", 2020, true, false);
        add("STU9012", "89012345678", "8AGLA13L0JL128945", "Renault", "Sandero", 2019, true, false);
        
        // Veículos com Recall
        add("VWX3456", "90123456789", "9BWDB45U5ET020689", "Volkswagen", "Virtus", 2021, false, true);
        add("YZA7890", "01234567890", "9BD178068M8054321", "Chevrolet", "Tracker", 2022, false, true);
        add("BCD1234", "11234567891", "8AFXZZF6ABJ143256", "Fiat", "Argo", 2020, false, true);
        
        // Veículos com Ambas Restrições
        add("EFG5678", "21234567892", "9BWAA45U59P098765", "Volkswagen", "T-Cross", 2019, true, true);
        add("HIJ9012", "31234567893", "93Y3SRFP4LJ200100", "Toyota", "Yaris", 2021, true, true);
        
        // Veículos Importados/Luxo
        add("KLM3456", "41234567894", "1HGCV1F30JA123456", "Honda", "Civic", 2020, false, false);
        add("NOP7890", "51234567895", "2HGFC2F59LH543210", "Honda", "Accord", 2021, false, false);
        add("QRS1234", "61234567896", "WBA3B3C55EF123456", "BMW", "320i", 2022, false, false);
        add("TUV5678", "71234567897", "WDD2050061F123456", "Mercedes-Benz", "C180", 2021, false, false);
        add("WXY9012", "81234567898", "WAUZZZ8V8KA123456", "Audi", "A3", 2019, false, false);
        
        // SUVs
        add("ZAB3456", "91234567899", "9BWDB75X2JT123456", "Volkswagen", "Tiguan", 2020, false, false);
        add("CDE7890", "10234567800", "9BD178088M8765432", "Chevrolet", "Equinox", 2021, false, false);
        add("FGH1234", "12234567801", "93Y3SRMG4MJ345678", "Toyota", "RAV4", 2022, false, false);
        add("IJK5678", "13234567802", "9BFXE45L0ML987654", "Jeep", "Compass", 2021, false, false);
        add("LMN9012", "14234567803", "9BFZH18K5NL456789", "Jeep", "Renegade", 2020, false, false);
        
        // Picapes
        add("OPQ3456", "15234567804", "8AFBR22L0MJ234567", "Fiat", "Toro", 2021, false, false);
        add("RST7890", "16234567805", "9BG186078J8345678", "Chevrolet", "S10", 2020, false, false);
        add("UVW1234", "17234567806", "93Y3SRDB4KJ456789", "Toyota", "SW4", 2022, false, false);
        add("XYZ5679", "18234567807", "9BWHE41J584567890", "Volkswagen", "Amarok", 2021, false, false);
        
        // Compactos
        add("ABC5680", "19234567808", "9BD178068M8678901", "Chevrolet", "Prisma", 2019, false, false);
        add("DEF1235", "20234567809", "8AFZZZ54CBJ789012", "Fiat", "Mobi", 2020, false, false);
        add("GHI7891", "21234567810", "9BWCA05W8FP890123", "Volkswagen", "Up!", 2021, false, false);
        add("JKL3457", "22234567811", "93YKR3GE0MJ901234", "Toyota", "Etios", 2020, false, false);
        
        // Sedans Médios
        add("MNO9013", "23234567812", "9BD147068E8012345", "Chevrolet", "Cruze", 2021, false, false);
        add("PQR1236", "24234567813", "8AGSR19L4ML123456", "Renault", "Logan", 2019, false, false);
        add("STU5681", "25234567814", "9BWAA45U59P234567", "Volkswagen", "Jetta", 2020, false, false);

        log.info("Base de dados inicializada com {} veículos", DATABASE.size() / 3);
    }

    private static void add(String placa, String renavam, String vin,
                           String marca, String modelo, int ano,
                           boolean renajud, boolean recall) {
        VehicleData data = VehicleData.builder()
            .placa(placa)
            .renavam(renavam)
            .vin(vin)
            .marca(marca)
            .modelo(modelo)
            .ano(ano)
            .renajud(renajud)
            .recall(recall)
            .build();

        // Índices por placa, renavam e vin
        DATABASE.put(placa.toUpperCase(), data);
        DATABASE.put(renavam, data);
        DATABASE.put(vin.toUpperCase(), data);
    }

    /**
     * Busca veículo por qualquer identificador (PLACA, RENAVAM ou VIN)
     */
    public Optional<VehicleData> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        VehicleData data = DATABASE.get(identifier.toUpperCase());
        
        if (data != null) {
            log.debug("Veículo encontrado: {} {} ({})", data.getMarca(), data.getModelo(), data.getVin());
        } else {
            log.warn("Veículo não encontrado para identificador: {}", identifier);
        }

        return Optional.ofNullable(data);
    }

    /**
     * Retorna estatísticas da base
     */
    public DatabaseStats getStats() {
        long totalVehicles = DATABASE.values().stream()
            .map(VehicleData::getVin)
            .distinct()
            .count();

        long withRenajud = DATABASE.values().stream()
            .filter(VehicleData::isRenajud)
            .map(VehicleData::getVin)
            .distinct()
            .count();

        long withRecall = DATABASE.values().stream()
            .filter(VehicleData::isRecall)
            .map(VehicleData::getVin)
            .distinct()
            .count();

        return new DatabaseStats(totalVehicles, withRenajud, withRecall);
    }

    @Data
    @AllArgsConstructor
    public static class DatabaseStats {
        private long totalVehicles;
        private long withRenajud;
        private long withRecall;
    }

    @Data
    @lombok.Builder
    public static class VehicleData {
        private String placa;
        private String renavam;
        private String vin;
        private String marca;
        private String modelo;
        private int ano;
        private boolean renajud;
        private boolean recall;
    }
}

