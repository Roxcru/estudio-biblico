
package com.estudiobiblico.service;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.estudiobiblico.model.Favorito;
import com.estudiobiblico.repository.FavoritoRepository;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final BibliaService bibliaService;

    public FavoritoService(
            FavoritoRepository favoritoRepository,
            BibliaService bibliaService) {
        this.favoritoRepository = favoritoRepository;
        this.bibliaService = bibliaService;
    }

    public Favorito crear(String usuarioId, Favorito favorito) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario autenticado es obligatorio");
        }

        validarReferencia(favorito);

        boolean existe =
                favoritoRepository
                        .existsByUsuarioIdAndVersionAndLibroAndCapituloAndVersiculo(
                                usuarioId,
                                favorito.getVersion(),
                                favorito.getLibro(),
                                favorito.getCapitulo(),
                                favorito.getVersiculo());

        if (existe) {
            throw new IllegalArgumentException(
                    "Este versículo ya está en tus favoritos");
        }

        favorito.setId(null);
        favorito.setUsuarioId(usuarioId);
        favorito.setVersion(favorito.getVersion().trim());
        favorito.setLibro(favorito.getLibro().trim());
        favorito.setFechaCreacion(Instant.now());

        try {
            return favoritoRepository.save(favorito);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException(
                    "Este versículo ya está en tus favoritos");
        }
    }

    public List<Favorito> listar(String usuarioId) {
        return favoritoRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public Favorito obtenerPorId(String id, String usuarioId) {
        return favoritoRepository
                .findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Favorito no encontrado"));
    }

    public void eliminar(String id, String usuarioId) {
        Favorito favorito = obtenerPorId(id, usuarioId);
        favoritoRepository.delete(favorito);
    }
    
    private void validarReferencia(Favorito favorito) {
        if (favorito == null) {
            throw new IllegalArgumentException(
                    "Debe enviar los datos del favorito");
        }

        bibliaService.validarReferencia(
                favorito.getVersion(),
                favorito.getLibro(),
                favorito.getCapitulo(),
                favorito.getVersiculo(),
                favorito.getVersiculo());
    }
}
