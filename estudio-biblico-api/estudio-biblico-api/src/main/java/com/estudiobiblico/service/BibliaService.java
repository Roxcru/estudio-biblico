package com.estudiobiblico.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class BibliaService {

    private final ObjectMapper objectMapper;

    @Value("${biblia.catalogo:./biblia/bibleBooks.json}")
    private String archivoCatalogo;

    @Value("${biblia.version-predeterminada:Reina Valera 1960}")
    private String versionPredeterminada;

    private JsonNode catalogo;

    private final Map<String, JsonNode> librosCache = new HashMap<>();

    public BibliaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void cargarCatalogo() {
        Path ruta = Path.of(archivoCatalogo)
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(ruta)) {
            throw new IllegalStateException(
                    "No se encontró el catálogo bíblico: " + ruta);
        }

        catalogo = objectMapper.readTree(ruta.toFile());

        if (catalogo == null
                || !catalogo.path("Versions").isArray()
                || catalogo.path("Versions").isEmpty()) {
            throw new IllegalStateException(
                    "El catálogo debe contener un arreglo 'Versions'");
        }

        // Verifica que exista la versión configurada por defecto.
        obtenerVersionNode(versionPredeterminada);
    }

    public String obtenerVersion() {
        return obtenerNombreVersion(versionPredeterminada);
    }

    public JsonNode listarVersiones() {
        return catalogo.path("Versions");
    }

    public JsonNode obtenerVersion(String version) {
        return obtenerVersionNode(version);
    }

    public JsonNode listarLibros() {
        return listarLibros(versionPredeterminada);
    }

    public JsonNode listarLibros(String version) {
        return obtenerVersionNode(version).path("Books");
    }

    public JsonNode obtenerLibro(String identificadorLibro) {
        return obtenerLibro(versionPredeterminada, identificadorLibro);
    }

    public JsonNode obtenerLibro(
            String version,
            String identificadorLibro) {

        JsonNode libroCatalogo = buscarLibroCatalogo(
                version, identificadorLibro);

        String rutaArchivo = libroCatalogo.path("Path").asText();

        if (rutaArchivo.isBlank()) {
            throw new IllegalStateException(
                    "El libro no tiene una ruta configurada en el catálogo");
        }

        Path ruta = Path.of(rutaArchivo)
                .toAbsolutePath()
                .normalize();

        String claveCache = ruta.toString();

        JsonNode libro = librosCache.get(claveCache);

        if (libro != null) {
            return libro;
        }

        if (!Files.isRegularFile(ruta)) {
            throw new IllegalStateException(
                    "No se encontró el archivo del libro bíblico: " + ruta);
        }

        libro = objectMapper.readTree(ruta.toFile());

        if (libro == null
                || !libro.path("Chapters").isArray()) {
            throw new IllegalStateException(
                    "El archivo del libro debe contener "
                            + "un arreglo 'Chapters': " + ruta);
        }

        librosCache.put(claveCache, libro);
        return libro;
    }

    public JsonNode obtenerCapitulo(
            String identificadorLibro,
            int numeroCapitulo) {

        return obtenerCapitulo(
                versionPredeterminada,
                identificadorLibro,
                numeroCapitulo);
    }

    public JsonNode obtenerCapitulo(
            String version,
            String identificadorLibro,
            int numeroCapitulo) {

        if (numeroCapitulo < 1) {
            throw new IllegalArgumentException(
                    "El capítulo debe ser mayor que cero");
        }

        JsonNode libro = obtenerLibro(version, identificadorLibro);

        for (JsonNode capitulo : libro.path("Chapters")) {
            if (capitulo.path("Chapter").asInt(-1)
                    == numeroCapitulo) {
                return capitulo;
            }
        }

        throw new IllegalArgumentException(
                "No existe el capítulo " + numeroCapitulo
                        + " en el libro " + identificadorLibro);
    }

    public void validarReferencia(
            String versionReferencia,
            String codigoLibro,
            Integer numeroCapitulo,
            Integer versiculoDesde,
            Integer versiculoHasta) {

        if (versionReferencia == null
                || versionReferencia.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar la versión bíblica");
        }

        if (codigoLibro == null || codigoLibro.isBlank()
                || numeroCapitulo == null || numeroCapitulo < 1
                || versiculoDesde == null || versiculoDesde < 1
                || versiculoHasta == null || versiculoHasta < 1) {
            throw new IllegalArgumentException(
                    "La referencia bíblica contiene datos inválidos");
        }

        if (versiculoHasta < versiculoDesde) {
            throw new IllegalArgumentException(
                    "El versículo final debe ser mayor o igual al inicial");
        }

        JsonNode capitulo = obtenerCapitulo(
                versionReferencia,
                codigoLibro,
                numeroCapitulo);

        JsonNode versiculos = capitulo.path("Verses");

        if (!versiculos.isArray() || versiculos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El capítulo " + numeroCapitulo
                            + " de " + codigoLibro
                            + " todavía no tiene versículos cargados");
        }

        Set<Integer> existentes = new HashSet<>();

        for (JsonNode versiculo : versiculos) {
            int numero = versiculo.path("Verse").asInt(-1);

            if (numero > 0) {
                existentes.add(numero);
            }
        }

        for (int numero = versiculoDesde;
                numero <= versiculoHasta;
                numero++) {

            if (!existentes.contains(numero)) {
                throw new IllegalArgumentException(
                        "No existe el versículo " + numero
                                + " en " + codigoLibro
                                + ", capítulo " + numeroCapitulo);
            }
        }
    }

    private JsonNode obtenerVersionNode(String identificadorVersion) {
        if (identificadorVersion == null
                || identificadorVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar la versión bíblica");
        }

        for (JsonNode version : catalogo.path("Versions")) {
            String nombre = version.path("Version").asText();

            if (coincide(nombre, identificadorVersion)
                    || String.valueOf(version.path("Code").asInt())
                            .equals(identificadorVersion.trim())) {
                return version;
            }
        }

        throw new IllegalArgumentException(
                "No existe la versión bíblica: " + identificadorVersion);
    }

    private String obtenerNombreVersion(String identificadorVersion) {
        return obtenerVersionNode(identificadorVersion)
                .path("Version").asText();
    }

    private JsonNode buscarLibroCatalogo(
            String version,
            String identificadorLibro) {

        if (identificadorLibro == null
                || identificadorLibro.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar el libro bíblico");
        }

        JsonNode libros = listarLibros(version);

        for (JsonNode libro : libros) {
            String nombre = libro.path("Name").asText();
            String menu = libro.path("Menu").asText();
            String ruta = libro.path("Path").asText();

            String nombreArchivo = "";

            if (!ruta.isBlank()) {
                nombreArchivo = Path.of(ruta)
                        .getFileName()
                        .toString()
                        .replaceFirst("(?i)\\.json$", "");
            }

            if (coincide(nombre, identificadorLibro)
                    || coincide(menu, identificadorLibro)
                    || coincide(nombreArchivo, identificadorLibro)) {
                return libro;
            }
        }

        throw new IllegalArgumentException(
                "No existe el libro '" + identificadorLibro
                        + "' en la versión " + version);
    }

    private boolean coincide(String valor, String buscado) {
        return valor != null
                && buscado != null
                && valor.trim().equalsIgnoreCase(buscado.trim());
    }
}
