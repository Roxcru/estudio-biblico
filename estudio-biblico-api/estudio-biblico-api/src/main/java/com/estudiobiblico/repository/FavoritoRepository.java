package com.estudiobiblico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.estudiobiblico.model.Favorito;

public interface FavoritoRepository
        extends MongoRepository<Favorito, String> {

    List<Favorito> findByUsuarioIdOrderByFechaCreacionDesc(
            String usuarioId);

    Optional<Favorito> findByIdAndUsuarioId(
            String id, String usuarioId);

    boolean existsByUsuarioIdAndVersionAndLibroAndCapituloAndVersiculo(
            String usuarioId,
            String version,
            String libro,
            int capitulo,
            int versiculo);
}
