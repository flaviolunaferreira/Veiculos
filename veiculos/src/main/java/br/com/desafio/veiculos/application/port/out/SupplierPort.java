package br.com.desafio.veiculos.application.port.out;

import br.com.desafio.veiculos.domain.SupplierResult;

public interface SupplierPort {
    SupplierResult<Object> fetchData(String vin);
    String getSupplierName();
}

