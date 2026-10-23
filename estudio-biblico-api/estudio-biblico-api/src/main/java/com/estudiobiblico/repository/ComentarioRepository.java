package com.estudiobiblico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.estudiobiblico.model.Comentario;

public interface ComentarioRepository
        extends MongoRepository<Comentario, String> {

    List<Comentario> findByUsuarioIdOrderByFechaActualizacionDesc(
            String usuarioId);

    List<Comentario> findByUsuarioIdAndVersionAndLibroAndCapituloAndVersiculoOrderByFechaCreacionDesc(
            String usuarioId,
            String version,
            String libro,
            int capitulo,
            int versiculo);

    Optional<Comentario> findByIdAndUsuarioId(
            String id, String usuarioId);
}
