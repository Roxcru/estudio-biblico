
package com.estudiobiblico.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estudiobiblico.model.Favorito;
import com.estudiobiblico.service.FavoritoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    public ResponseEntity<Favorito> crear(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Favorito favorito) {

        Favorito creado = favoritoService.crear(
                jwt.getSubject(), favorito);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Favorito>> listar(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                favoritoService.listar(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Favorito> obtenerPorId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        return ResponseEntity.ok(
                favoritoService.obtenerPorId(id, jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        favoritoService.eliminar(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
