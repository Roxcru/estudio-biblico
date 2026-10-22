package com.estudiobiblico.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.estudiobiblico.model.TemaEstudio;

public interface TemaEstudioRepository
        extends MongoRepository<TemaEstudio, String> {

    // Listar todos los temas de un usuario
    // ordenados por última actualización
    java.util.List<TemaEstudio>
        findByUsuarioIdOrderByFechaActualizacionDesc(String usuarioId);

    // Listar temas de un usuario con paginación
    Page<TemaEstudio>
        findByUsuarioIdOrderByFechaActualizacionDesc(
            String usuarioId,
            Pageable pageable
        );
}