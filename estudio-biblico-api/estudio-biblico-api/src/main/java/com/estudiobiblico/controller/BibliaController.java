
package com.estudiobiblico.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.estudiobiblico.service.BibliaService;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/biblia")
public class BibliaController {

    private final BibliaService bibliaService;

    public BibliaController(BibliaService bibliaService) {
        this.bibliaService = bibliaService;
    }

    // Obtener la versión bíblica predeterminada
    @GetMapping("/version")
    public Map<String, String> obtenerVersion() {
        return Map.of(
                "version",
                bibliaService.obtenerVersion()
        );
    }

    // Listar los libros de la Biblia
    @GetMapping("/libros")
    public JsonNode listarLibros() {
        return bibliaService.listarLibros();
    }

    // Obtener un libro bíblico
    @GetMapping("/libros/{codigoLibro}")
    public JsonNode obtenerLibro(
            @PathVariable String codigoLibro) {

        return bibliaService.obtenerLibro(codigoLibro);
    }

    // Obtener un capítulo de un libro bíblico
    @GetMapping("/libros/{codigoLibro}/capitulos/{numeroCapitulo}")
    public JsonNode obtenerCapitulo(
            @PathVariable String codigoLibro,
            @PathVariable int numeroCapitulo) {

        return bibliaService.obtenerCapitulo(
                codigoLibro,
                numeroCapitulo
        );
    }

    // Validar una referencia bíblica
    @GetMapping("/validar")
    public Map<String, Object> validarReferencia(
            @RequestParam(defaultValue = "Reina Valera 1960")
            String version,

            @RequestParam String libro,

            @RequestParam Integer capitulo,

            @RequestParam Integer versiculoDesde,

            @RequestParam Integer versiculoHasta) {

        bibliaService.validarReferencia(
                version,
                libro,
                capitulo,
                versiculoDesde,
                versiculoHasta
        );

        return Map.of(
                "valida", true,
                "mensaje", "La referencia bíblica es válida"
        );
    }
}
