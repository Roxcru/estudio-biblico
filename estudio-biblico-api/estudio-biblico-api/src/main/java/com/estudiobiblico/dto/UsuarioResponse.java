
package com.estudiobiblico.dto;

import java.time.Instant;

import com.estudiobiblico.model.Usuario;

public record UsuarioResponse(
        String id,
        String nombre,
        String correo,
        boolean activo,
        Instant fechaCreacion
) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.isActivo(),
                usuario.getFechaCreacion()
        );
    }
}
