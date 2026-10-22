package com.estudiobiblico.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
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

import com.estudiobiblico.dto.RespuestaPaginadaDTO;
import com.estudiobiblico.dto.TemaEstudioResumenDTO;
import com.estudiobiblico.model.ReferenciaVersiculo;
import com.estudiobiblico.model.TemaEstudio;
import com.estudiobiblico.service.TemaEstudioService;

import jakarta.validation.Valid;
import com.estudiobiblico.dto.ActualizarTemaRequest;

@RestController
@RequestMapping("/api/temas")
public class TemaEstudioController {

    private final TemaEstudioService temaEstudioService;

    public TemaEstudioController(
            TemaEstudioService temaEstudioService) {
        this.temaEstudioService = temaEstudioService;
    }

    // Crear un tema: el usuario se obtiene del JWT.
    @PostMapping
    public TemaEstudio crear(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody TemaEstudio tema) {

        tema.setUsuarioId(jwt.getSubject());

        return temaEstudioService.crear(tema);
    }

    // Listar los temas del usuario autenticado.
    @GetMapping
    public List<TemaEstudio> listarPorUsuario(
            @AuthenticationPrincipal Jwt jwt) {

        return temaEstudioService.listarPorUsuario(
                jwt.getSubject());
    }

    // Obtener un tema verificando su propietario.
    @GetMapping("/{id}")
    public TemaEstudio obtenerPorId(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        return temaEstudioService.obtenerPorIdDeUsuario(
                id, jwt.getSubject());
    }

    // Actualizar los datos generales de un tema.
    @PutMapping("/{id}")
    public TemaEstudio actualizar(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ActualizarTemaRequest datos) {

        return temaEstudioService.actualizar(
                id,
                datos,
                jwt.getSubject());
    }

    // Agregar una referencia bíblica.
    @PostMapping("/{id}/versiculos")
    public TemaEstudio agregarVersiculo(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReferenciaVersiculo referencia) {

        return temaEstudioService.agregarVersiculo(
                id, referencia, jwt.getSubject());
    }

    // Eliminar un tema.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        temaEstudioService.eliminar(
                id, jwt.getSubject());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Tema de estudio eliminado correctamente",
                "id", id
        ));
    }

    // Actualizar una referencia bíblica.
    @PutMapping("/{id}/versiculos/{orden}")
    public TemaEstudio actualizarVersiculo(
            @PathVariable String id,
            @PathVariable Integer orden,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReferenciaVersiculo referencia) {

        return temaEstudioService.actualizarVersiculo(
                id, orden, referencia, jwt.getSubject());
    }

    // Eliminar una referencia bíblica.
    @DeleteMapping("/{id}/versiculos/{orden}")
    public Map<String, Object> eliminarVersiculo(
            @PathVariable String id,
            @PathVariable Integer orden,
            @AuthenticationPrincipal Jwt jwt) {

        temaEstudioService.eliminarVersiculo(
                id, orden, jwt.getSubject());

        return Map.of(
                "mensaje", "Referencia bíblica eliminada correctamente",
                "temaId", id,
                "orden", orden
        );
    }

    // Obtener el contenido bíblico completo del tema.
    @GetMapping("/{id}/contenido")
    public Map<String, Object> obtenerContenido(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        return temaEstudioService.obtenerContenido(
                id, jwt.getSubject());
    }

    // Reordenar las referencias bíblicas.
    @PutMapping("/{id}/versiculos/orden")
    public TemaEstudio reordenarVersiculos(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<Integer> nuevoOrden) {

        return temaEstudioService.reordenarVersiculos(
                id, nuevoOrden, jwt.getSubject());
    }

    // Obtener el contenido de una referencia específica.
    @GetMapping("/{id}/versiculos/{orden}")
    public Map<String, Object> obtenerContenidoReferencia(
            @PathVariable String id,
            @PathVariable Integer orden,
            @AuthenticationPrincipal Jwt jwt) {

        return temaEstudioService.obtenerContenidoReferencia(
                id, orden, jwt.getSubject());
    }

    // Insertar una referencia en una posición determinada.
    @PostMapping("/{id}/versiculos/insertar/{posicion}")
    public TemaEstudio insertarVersiculo(
            @PathVariable String id,
            @PathVariable Integer posicion,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReferenciaVersiculo referencia) {

        return temaEstudioService.insertarVersiculo(
                id, posicion, referencia, jwt.getSubject());
    }

    // Listar las referencias de un tema.
    @GetMapping("/{id}/versiculos")
    public List<ReferenciaVersiculo> listarVersiculos(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        return temaEstudioService.listarVersiculos(
                id, jwt.getSubject());
    }

    // Listar temas paginados del usuario autenticado.
    @GetMapping("/paginados")
    public RespuestaPaginadaDTO<TemaEstudioResumenDTO>
            listarPorUsuarioPaginado(
                    @AuthenticationPrincipal Jwt jwt,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "10") int size) {

        Page<TemaEstudio> resultado =
                temaEstudioService.listarPorUsuarioPaginado(
                        jwt.getSubject(), page, size);

        List<TemaEstudioResumenDTO> contenido =
                resultado.getContent()
                        .stream()
                        .map(tema -> new TemaEstudioResumenDTO(
                                tema.getId(),
                                tema.getTitulo(),
                                tema.getUsuarioId(),
                                tema.getEstado()
                        ))
                        .toList();

        return new RespuestaPaginadaDTO<>(
                contenido,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages(),
                resultado.isLast()
        );
    }
}