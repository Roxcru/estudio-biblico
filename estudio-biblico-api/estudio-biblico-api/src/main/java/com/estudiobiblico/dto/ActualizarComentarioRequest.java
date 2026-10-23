
package com.estudiobiblico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarComentarioRequest(
        @NotBlank(message = "El contenido del comentario es obligatorio")
        @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
        String contenido
) {
}
