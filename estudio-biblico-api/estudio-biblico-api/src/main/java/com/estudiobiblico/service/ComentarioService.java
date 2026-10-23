
package com.estudiobiblico.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.estudiobiblico.model.Comentario;
import com.estudiobiblico.repository.ComentarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final BibliaService bibliaService;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            BibliaService bibliaService) {
        this.comentarioRepository = comentarioRepository;
        this.bibliaService = bibliaService;
    }

    public Comentario crear(String usuarioId, Comentario comentario) {
        validarComentario(comentario);

        comentario.setId(null);
        comentario.setUsuarioId(usuarioId);
        comentario.setVersion(comentario.getVersion().trim());
        comentario.setLibro(comentario.getLibro().trim());

        Instant ahora = Instant.now();
        comentario.setFechaCreacion(ahora);
        comentario.setFechaActualizacion(ahora);

        return comentarioRepository.save(comentario);
    }

    public List<Comentario> listar(String usuarioId) {
        return comentarioRepository
                .findByUsuarioIdOrderByFechaActualizacionDesc(usuarioId);
    }

    public List<Comentario> listarPorVersiculo(
            String usuarioId,
            String version,
            String libro,
            int capitulo,
            int versiculo) {

        return comentarioRepository
                .findByUsuarioIdAndVersionAndLibroAndCapituloAndVersiculoOrderByFechaCreacionDesc(
                        usuarioId, version, libro, capitulo, versiculo);
    }

    public Comentario obtenerPorId(String id, String usuarioId) {
        return comentarioRepository
                .findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Comentario no encontrado"));
    }

    public Comentario actualizar(
            String id,
            String usuarioId,
            String contenido) {

        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido del comentario es obligatorio");
        }

        if (contenido.length() > 2000) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar los 2000 caracteres");
        }

        Comentario comentario = obtenerPorId(id, usuarioId);
        comentario.setContenido(contenido.trim());
        comentario.setFechaActualizacion(Instant.now());

        return comentarioRepository.save(comentario);
    }

    public void eliminar(String id, String usuarioId) {
        Comentario comentario = obtenerPorId(id, usuarioId);
        comentarioRepository.delete(comentario);
    }

    private void validarComentario(Comentario comentario) {
        if (comentario == null) {
            throw new IllegalArgumentException(
                    "Debe enviar los datos del comentario");
        }

        if (comentario.getContenido() == null
                || comentario.getContenido().isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido del comentario es obligatorio");
        }

        if (comentario.getContenido().length() > 2000) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar los 2000 caracteres");
        }

        bibliaService.validarReferencia(
                comentario.getVersion(),
                comentario.getLibro(),
                comentario.getCapitulo(),
                comentario.getVersiculo(),
                comentario.getVersiculo());
    }

}
