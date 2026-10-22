package com.estudiobiblico.dto;
import java.util.List;

public record RespuestaPaginadaDTO<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean ultimaPagina
) {
}