
package com.estudiobiblico.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // Obtener la version biblica predeterminada
    @GetMapping("/version")
    public Map<String, String> obtenerVersion() {
        return Map.of(
                "version",
                bibliaService.obtenerVersion()
        );
    }

    // Listar las versiones disponibles
    @GetMapping("/versiones")
    public List<Map<String, Object>> listarVersiones() {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (JsonNode version : bibliaService.listarVersiones()) {
            Map<String, Object> item = new HashMap<>();
            item.put("codigo", version.path("Code").asInt());
            item.put("version", version.path("Version").asText());

            resultado.add(item);
        }

        return resultado;
    }

    // Listar libros de la version seleccionada
    @GetMapping("/libros")
    public JsonNode listarLibros(
            @RequestParam(defaultValue = "Reina Valera 1960")
            String version) {

        return bibliaService.listarLibros(version);
    }

    // Obtener un libro de la version seleccionada
    @GetMapping("/libros/{codigoLibro}")
    public JsonNode obtenerLibro(
            @PathVariable String codigoLibro,
            @RequestParam(defaultValue = "Reina Valera 1960")
            String version) {

        return bibliaService.obtenerLibro(
                version,
                codigoLibro
        );
    }

    // Obtener un capitulo de la version seleccionada
    @GetMapping("/libros/{codigoLibro}/capitulos/{numeroCapitulo}")
    public JsonNode obtenerCapitulo(
            @PathVariable String codigoLibro,
            @PathVariable int numeroCapitulo,
            @RequestParam(defaultValue = "Reina Valera 1960")
            String version) {

        return bibliaService.obtenerCapitulo(
                version,
                codigoLibro,
                numeroCapitulo
        );
    }

    // Validar una referencia biblica
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
                "mensaje", "La referencia biblica es valida"
        );
    }
}
