package com.estudiobiblico.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReferenciaVersiculo(
        @NotBlank String version,
        @NotBlank String libro,
        @NotNull @Min(1) Integer capitulo,
        @NotNull @Min(1) Integer versiculoDesde,
        @NotNull @Min(1) Integer versiculoHasta,
        Integer orden,
        String nota
) {
    public ReferenciaVersiculo {
        if (versiculoDesde != null
                && versiculoHasta != null
                && versiculoHasta < versiculoDesde) {
            throw new IllegalArgumentException(
                    "El versículo hasta no puede ser menor que el versículo desde"
            );
        }
    }
}