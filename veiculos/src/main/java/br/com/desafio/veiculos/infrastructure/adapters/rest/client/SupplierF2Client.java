package br.com.desafio.veiculos.infrastructure.adapters.rest.client;

import br.com.desafio.veiculos.domain.f2.F2ResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "supplierF2", url = "${suppliers.f2.url}")
public interface SupplierF2Client {

    @GetMapping("/vehicle/{vin}")
    F2ResponseData getVehicleData(@PathVariable("vin") String vin);
}
