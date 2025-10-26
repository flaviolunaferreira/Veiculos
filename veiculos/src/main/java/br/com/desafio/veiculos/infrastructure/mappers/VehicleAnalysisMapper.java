package br.com.desafio.veiculos.infrastructure.mappers;

import br.com.desafio.veiculos.domain.Constraints;
import br.com.desafio.veiculos.domain.Infractions;
import br.com.desafio.veiculos.domain.VehicleAnalysis;
import br.com.desafio.veiculos.domain.f1.F1ResponseData;
import br.com.desafio.veiculos.domain.f2.F2ResponseData;
import br.com.desafio.veiculos.domain.f3.F3ResponseData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleAnalysisMapper {

    public Constraints mapF1Constraints(F1ResponseData f1Data) {
        if (f1Data == null || f1Data.restricoes() == null) {
            return null;
        }
        return f1Data.restricoes();
    }

    public Constraints mapF2Constraints(F2ResponseData f2Data, Constraints existingConstraints) {
        boolean renajud = (existingConstraints != null && existingConstraints.renajud()) ||
                          (f2Data != null && "ATIVO".equalsIgnoreCase(f2Data.statusRenajud()));

        boolean recall = (existingConstraints != null && existingConstraints.recall()) ||
                         (f2Data != null && f2Data.detalheRecall() != null && !f2Data.detalheRecall().isBlank());

        return new Constraints(renajud, recall);
    }

    public Infractions mapF3Infractions(F3ResponseData f3Data) {
        if (f3Data == null) {
            return null;
        }

        List<Infractions.InfractionDetail> details = f3Data.details() != null
            ? f3Data.details().stream()
                .map(d -> new Infractions.InfractionDetail(d.description(), d.amount()))
                .collect(Collectors.toList())
            : Collections.emptyList();

        return new Infractions(f3Data.totalAmount(), details);
    }

    public void mergeAnalysisData(VehicleAnalysis.Builder builder, Object data) {
        if (data instanceof F1ResponseData f1) {
            builder.constraints(mapF1Constraints(f1));
        } else if (data instanceof F3ResponseData f3) {
            builder.infractions(mapF3Infractions(f3));
        } else if (data instanceof F2ResponseData f2) {
            // O merge de F2 deve considerar os dados já existentes de F1
            builder.constraints(mapF2Constraints(f2, builder.build().constraints()));
        }
    }
}
