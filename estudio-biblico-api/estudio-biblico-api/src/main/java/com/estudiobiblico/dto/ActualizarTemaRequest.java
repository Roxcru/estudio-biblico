package com.estudiobiblico.dto;

public record ActualizarTemaRequest(
        String titulo,
        String descripcion,
        String introduccion,
        String conclusion,
        String estado,
        boolean esPublico
) {
}