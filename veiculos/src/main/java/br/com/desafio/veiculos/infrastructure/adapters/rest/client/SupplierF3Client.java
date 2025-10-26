package br.com.desafio.veiculos.infrastructure.adapters.rest.client;

import br.com.desafio.veiculos.domain.f3.F3ResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "supplierF3", url = "${suppliers.f3.url}")
public interface SupplierF3Client {

    @GetMapping("/infractions/{vin}")
    F3ResponseData getInfractions(@PathVariable("vin") String vin);
}

