
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.estudiobiblico.dto.ActualizarComentarioRequest;
import com.estudiobiblico.model.Comentario;
import com.estudiobiblico.service.ComentarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PostMapping
    public ResponseEntity<Comentario> crear(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Comentario comentario) {

        Comentario creado = comentarioService.crear(
                jwt.getSubject(), comentario);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Comentario>> listar(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                comentarioService.listar(jwt.getSubject()));
    }

    @GetMapping("/versiculo")
    public ResponseEntity<List<Comentario>> listarPorVersiculo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String version,
            @RequestParam String libro,
            @RequestParam int capitulo,
            @RequestParam int versiculo) {

        return ResponseEntity.ok(
                comentarioService.listarPorVersiculo(
                        jwt.getSubject(), version, libro,
                        capitulo, versiculo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comentario> obtenerPorId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        return ResponseEntity.ok(
                comentarioService.obtenerPorId(id, jwt.getSubject()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comentario> actualizar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody ActualizarComentarioRequest request) {

        return ResponseEntity.ok(
                comentarioService.actualizar(
                        id, jwt.getSubject(), request.contenido()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        comentarioService.eliminar(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
