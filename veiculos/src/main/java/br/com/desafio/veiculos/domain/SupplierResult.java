package br.com.desafio.veiculos.domain;

import br.com.desafio.veiculos.domain.SupplierStatus.Status;

public record SupplierResult<T>(
    SupplierStatus status,
    T data
) {
    public static <T> SupplierResult<T> success(String supplierName, long latencyMs, T data) {
        return new SupplierResult<>(new SupplierStatus(Status.SUCCESS, latencyMs, null), data);
    }

    public static <T> SupplierResult<T> failure(String supplierName, long latencyMs, String error) {
        return new SupplierResult<>(new SupplierStatus(Status.FAILURE, latencyMs, error), null);
    }

    public static <T> SupplierResult<T> timeout(String supplierName, long latencyMs) {
        return new SupplierResult<>(new SupplierStatus(Status.TIMEOUT, latencyMs, "Supplier timed out"), null);
    }

    public static <T> SupplierResult<T> notCalled(String supplierName) {
        return new SupplierResult<>(new SupplierStatus(Status.NOT_CALLED, 0, null), null);
    }
}

