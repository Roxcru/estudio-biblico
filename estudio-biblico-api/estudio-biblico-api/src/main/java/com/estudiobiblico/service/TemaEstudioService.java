
package com.estudiobiblico.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.estudiobiblico.model.ReferenciaVersiculo;
import com.estudiobiblico.model.TemaEstudio;
import com.estudiobiblico.repository.TemaEstudioRepository;

import tools.jackson.databind.JsonNode;
import com.estudiobiblico.dto.ActualizarTemaRequest;

@Service
public class TemaEstudioService {

        private final TemaEstudioRepository repository;
        private final BibliaService bibliaService;

        public TemaEstudioService(
                TemaEstudioRepository repository,
                BibliaService bibliaService) {
                this.repository = repository;
                this.bibliaService = bibliaService;
        }

        // Crear un tema de estudio
        public TemaEstudio crear(TemaEstudio tema) {

                if (tema == null) {
                throw new IllegalArgumentException(
                        "El tema de estudio no puede ser nulo");
                }

                if (tema.getUsuarioId() == null
                        || tema.getUsuarioId().isBlank()) {
                throw new IllegalArgumentException(
                        "Debe indicar el usuario del tema");
                }

                if (tema.getTitulo() == null
                        || tema.getTitulo().isBlank()) {
                throw new IllegalArgumentException(
                        "El título del tema es obligatorio");
                }

                tema.setId(null);

                if (tema.getVersiculos() == null) {
                tema.setVersiculos(new ArrayList<>());
                }

                // Validar todas las referencias antes de guardar.
                validarReferencias(tema.getVersiculos());

                Instant ahora = Instant.now();
                tema.setFechaCreacion(ahora);
                tema.setFechaActualizacion(ahora);

                if (tema.getEstado() == null
                        || tema.getEstado().isBlank()) {
                tema.setEstado("BORRADOR");
                }

                return repository.save(tema);
        }

        // Listar los temas de un usuario
        public List<TemaEstudio> listarPorUsuario(String usuarioId) {

                if (usuarioId == null || usuarioId.isBlank()) {
                throw new IllegalArgumentException(
                        "Debe indicar el usuario");
                }

                return repository
                        .findByUsuarioIdOrderByFechaActualizacionDesc(
                                usuarioId);
        }

        // Obtener un tema por su ID
        private TemaEstudio obtenerPorId(String id) {

                if (id == null || id.isBlank()) {
                        throw new IllegalArgumentException(
                                "Debe indicar el ID del tema");
                }

                // Validar el formato del ObjectId de MongoDB
                if (!ObjectId.isValid(id)) {
                        throw new IllegalArgumentException(
                                "El ID del tema no tiene un formato válido");
                }

                // Buscar el tema en MongoDB
                return repository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Tema no encontrado"));
        }

        public TemaEstudio obtenerPorIdDeUsuario(
                String temaId,
                String usuarioId) {

                if (usuarioId == null || usuarioId.isBlank()) {
                        throw new IllegalArgumentException(
                                "Debe indicar el usuario autenticado");
                }

                TemaEstudio tema = obtenerPorId(temaId);

                if (!Objects.equals(tema.getUsuarioId(), usuarioId)) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                "No tienes permiso para acceder a este tema");
                }

                return tema;
        }

        // Eliminar un tema de estudio
        public void eliminar(String temaId, String usuarioId){
                // Verificar que el tema exista
                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                // Eliminar el tema de MongoDB
                repository.delete(tema);
        }

        // Agregar una referencia bíblica a un tema existente

        public TemaEstudio agregarVersiculo(
                String temaId,
                ReferenciaVersiculo referencia,
                String usuarioId){

                if (referencia == null) {
                        throw new IllegalArgumentException(
                                "La referencia bíblica no puede ser nula");
                }

                // Validar la referencia contra el JSON externo de la Biblia.
                validarReferencias(List.of(referencia));

                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                List<ReferenciaVersiculo> referencias =
                        new ArrayList<>(tema.getVersiculos());

                // Evitar referencias duplicadas.
                boolean duplicada = referencias.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(actual ->
                                Objects.equals(actual.version(), referencia.version())
                                && Objects.equals(actual.libro(), referencia.libro())
                                && Objects.equals(actual.capitulo(), referencia.capitulo())
                                && Objects.equals(actual.versiculoDesde(),
                                        referencia.versiculoDesde())
                                && Objects.equals(actual.versiculoHasta(),
                                        referencia.versiculoHasta()));

                if (duplicada) {
                        throw new IllegalArgumentException(
                                "La referencia ya existe en el tema");
                }

                // Agregar la referencia conservando todos sus datos.
                referencias.add(new ReferenciaVersiculo(
                        referencia.version(),
                        referencia.libro(),
                        referencia.capitulo(),
                        referencia.versiculoDesde(),
                        referencia.versiculoHasta(),
                        referencias.size() + 1,
                        referencia.nota()
                ));

                // Normalizar el orden de todas las referencias.
                renumerarReferencias(referencias);

                tema.setVersiculos(referencias);
                tema.setFechaActualizacion(Instant.now());

                return repository.save(tema);
        }

    // Validar todas las referencias de una lista
    private void validarReferencias(
            List<ReferenciaVersiculo> referencias) {

        if (referencias == null) {
            return;
        }

        for (ReferenciaVersiculo referencia : referencias) {

            if (referencia == null) {
                throw new IllegalArgumentException(
                        "La referencia bíblica no puede ser nula");
            }

            bibliaService.validarReferencia(
                    referencia.version(),
                    referencia.libro(),
                    referencia.capitulo(),
                    referencia.versiculoDesde(),
                    referencia.versiculoHasta()
            );
        }
    }

    
        // Actualizar los datos generales de un tema
         public TemaEstudio actualizar(
                String temaId,
                ActualizarTemaRequest datos,
                String usuarioId) {

                // Comprobar primero que el usuario sea propietario.
                TemaEstudio tema =
                        obtenerPorIdDeUsuario(temaId, usuarioId);

                if (datos == null) {
                        throw new IllegalArgumentException(
                                "Los datos del tema no pueden ser nulos");
                }

                if (datos.titulo() == null || datos.titulo().isBlank()) {
                        throw new IllegalArgumentException(
                                "El título del tema es obligatorio");
                }

                // Actualizar únicamente los campos permitidos.
                tema.setTitulo(datos.titulo().trim());
                tema.setDescripcion(datos.descripcion());
                tema.setIntroduccion(datos.introduccion());
                tema.setConclusion(datos.conclusion());

                if (datos.estado() != null && !datos.estado().isBlank()) {
                        tema.setEstado(datos.estado().trim());
                }

                tema.setEsPublico(datos.esPublico());

                // Conservar el propietario, el ID y la fecha de creación.
                tema.setFechaActualizacion(Instant.now());

                return repository.save(tema);
        }

        // Actualizar una referencia bíblica existente
        public TemaEstudio actualizarVersiculo(
                String temaId,
                Integer orden,
                ReferenciaVersiculo referencia,
                String usuarioId){

                if (orden == null || orden < 1) {
                        throw new IllegalArgumentException(
                                "El orden de la referencia debe ser mayor que cero");
                }

                if (referencia == null) {
                        throw new IllegalArgumentException(
                                "La referencia bíblica no puede ser nula");
                }

                validarReferencias(List.of(referencia));

                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                List<ReferenciaVersiculo> referencias =
                        new ArrayList<>(tema.getVersiculos());

                int indice = -1;

                for (int i = 0; i < referencias.size(); i++) {
                        ReferenciaVersiculo actual = referencias.get(i);

                        if (actual != null
                                && Objects.equals(actual.orden(), orden)) {
                        indice = i;
                        break;
                        }
                }

                if (indice == -1) {
                        throw new IllegalArgumentException(
                                "No existe una referencia con el orden indicado");
                }

                boolean duplicada = referencias.stream()
                        .filter(Objects::nonNull)
                        .filter(v -> !Objects.equals(v.orden(), orden))
                        .anyMatch(v ->
                                Objects.equals(v.version(), referencia.version())
                                && Objects.equals(v.libro(), referencia.libro())
                                && Objects.equals(v.capitulo(), referencia.capitulo())
                                && Objects.equals(v.versiculoDesde(),
                                        referencia.versiculoDesde())
                                && Objects.equals(v.versiculoHasta(),
                                        referencia.versiculoHasta()));

                if (duplicada) {
                        throw new IllegalArgumentException(
                                "La referencia ya existe en el tema");
                }

                ReferenciaVersiculo actualizada = new ReferenciaVersiculo(
                        referencia.version(),
                        referencia.libro(),
                        referencia.capitulo(),
                        referencia.versiculoDesde(),
                        referencia.versiculoHasta(),
                        orden,
                        referencia.nota()
                );

                referencias.set(indice, actualizada);

                tema.setVersiculos(referencias);
                tema.setFechaActualizacion(Instant.now());

                return repository.save(tema);
        }

        // Eliminar una referencia bíblica individual
        public TemaEstudio eliminarVersiculo(
                String temaId,
                Integer orden,
                String usuarioId){

                if (orden == null || orden < 1) {
                        throw new IllegalArgumentException(
                                "El orden de la referencia debe ser mayor que cero");
                }

                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                List<ReferenciaVersiculo> referencias =
                        new ArrayList<>(tema.getVersiculos());

                boolean eliminada = referencias.removeIf(
                        referencia -> referencia != null
                                && Objects.equals(referencia.orden(), orden));

                if (!eliminada) {
                        throw new IllegalArgumentException(
                                "No existe una referencia con el orden indicado");
                }

                // Renumerar después de eliminar.
                renumerarReferencias(referencias);

                tema.setVersiculos(referencias);
                tema.setFechaActualizacion(Instant.now());

                return repository.save(tema);
        }

        private void renumerarReferencias(
                List<ReferenciaVersiculo> referencias) {

                for (int i = 0; i < referencias.size(); i++) {
                        ReferenciaVersiculo referencia = referencias.get(i);

                        referencias.set(i, new ReferenciaVersiculo(
                                referencia.version(),
                                referencia.libro(),
                                referencia.capitulo(),
                                referencia.versiculoDesde(),
                                referencia.versiculoHasta(),
                                i + 1,
                                referencia.nota()
                        ));
                }
        }

        public Map<String, Object> obtenerContenido(String temaId, String usuarioId){
                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                Map<String, Object> resultado = new LinkedHashMap<>();
                resultado.put("id", tema.getId());
                resultado.put("usuarioId", tema.getUsuarioId());
                resultado.put("titulo", tema.getTitulo());
                resultado.put("descripcion", tema.getDescripcion());
                resultado.put("introduccion", tema.getIntroduccion());
                resultado.put("conclusion", tema.getConclusion());
                resultado.put("estado", tema.getEstado());
                resultado.put("esPublico", tema.isEsPublico());
                resultado.put("fechaCreacion", tema.getFechaCreacion());
                resultado.put("fechaActualizacion", tema.getFechaActualizacion());

                // Ordenar las referencias según el orden guardado en el tema.
                List<ReferenciaVersiculo> referencias =
                        new ArrayList<>(tema.getVersiculos());

                referencias.sort(Comparator.comparing(
                        ReferenciaVersiculo::orden,
                        Comparator.nullsLast(Integer::compareTo)
                ));

                List<Map<String, Object>> contenidoReferencias =
                        new ArrayList<>();

                for (ReferenciaVersiculo referencia : referencias) {
                        // Validar que la referencia bíblica exista.
                        bibliaService.validarReferencia(
                                referencia.version(),
                                referencia.libro(),
                                referencia.capitulo(),
                                referencia.versiculoDesde(),
                                referencia.versiculoHasta()
                        );

                        // Obtener el capítulo desde el JSON externo.
                        JsonNode capitulo = bibliaService.obtenerCapitulo(
                                referencia.version(),
                                referencia.libro(),
                                referencia.capitulo()
                        );

                        // El JSON utiliza la propiedad "Verses".
                        JsonNode versiculosBiblia = capitulo.path("Verses");

                        List<Map<String, Object>> versiculosSeleccionados =
                                new ArrayList<>();

                        // Extraer únicamente los versículos del rango solicitado.
                        for (JsonNode versiculo : versiculosBiblia) {
                        int numero = versiculo.path("Verse").asInt(-1);

                        if (numero >= referencia.versiculoDesde()
                                && numero <= referencia.versiculoHasta()) {

                                Map<String, Object> detalleVersiculo =
                                        new LinkedHashMap<>();

                                detalleVersiculo.put("numero", numero);
                                detalleVersiculo.put(
                                        "texto",
                                        versiculo.path("Text").asText()
                                );

                                versiculosSeleccionados.add(detalleVersiculo);
                        }
                        }

                        // Construir el resultado de cada referencia.
                        Map<String, Object> detalleReferencia =
                                new LinkedHashMap<>();

                        detalleReferencia.put("orden", referencia.orden());
                        detalleReferencia.put("version", referencia.version());
                        detalleReferencia.put("libro", referencia.libro());
                        detalleReferencia.put("capitulo", referencia.capitulo());
                        detalleReferencia.put(
                                "versiculoDesde",
                                referencia.versiculoDesde()
                        );
                        detalleReferencia.put(
                                "versiculoHasta",
                                referencia.versiculoHasta()
                        );
                        detalleReferencia.put("nota", referencia.nota());
                        detalleReferencia.put(
                                "versiculos",
                                versiculosSeleccionados
                        );

                        contenidoReferencias.add(detalleReferencia);
                }

                resultado.put("referencias", contenidoReferencias);

                return resultado;
        }

        public TemaEstudio reordenarVersiculos(
                String temaId,
                List<Integer> nuevoOrden,
                String usuarioId){

                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                List<ReferenciaVersiculo> referencias =
                        new ArrayList<>(tema.getVersiculos());

                if (nuevoOrden == null
                        || nuevoOrden.size() != referencias.size()) {
                        throw new IllegalArgumentException(
                                "Debes indicar el orden de todas las referencias."
                        );
                }

                Map<Integer, ReferenciaVersiculo> referenciasPorOrden =
                        new LinkedHashMap<>();

                for (ReferenciaVersiculo referencia : referencias) {
                        if (referencia.orden() == null
                                || referenciasPorOrden.containsKey(referencia.orden())) {
                        throw new IllegalStateException(
                                "Las referencias tienen órdenes nulos o duplicados."
                        );
                        }

                        referenciasPorOrden.put(
                                referencia.orden(),
                                referencia
                        );
                }

                if (nuevoOrden.stream().anyMatch(
                        orden -> orden == null
                                || !referenciasPorOrden.containsKey(orden))
                        || nuevoOrden.stream().distinct().count()
                                != nuevoOrden.size()) {
                        throw new IllegalArgumentException(
                                "El orden contiene posiciones inexistentes o repetidas."
                        );
                }

                List<ReferenciaVersiculo> referenciasReordenadas =
                        new ArrayList<>();

                for (int i = 0; i < nuevoOrden.size(); i++) {
                        ReferenciaVersiculo referencia =
                                referenciasPorOrden.get(nuevoOrden.get(i));

                        referenciasReordenadas.add(
                                new ReferenciaVersiculo(
                                        referencia.version(),
                                        referencia.libro(),
                                        referencia.capitulo(),
                                        referencia.versiculoDesde(),
                                        referencia.versiculoHasta(),
                                        i + 1,
                                        referencia.nota()
                                )
                        );
                }

                tema.setVersiculos(referenciasReordenadas);
                tema.setFechaActualizacion(Instant.now());

                return repository.save(tema);
        }

        public Map<String, Object> obtenerContenidoReferencia(
                String temaId,
                Integer orden,
                String usuarioId){

        // Buscar el tema
        TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

        // Buscar la referencia según su orden
        ReferenciaVersiculo referencia = tema.getVersiculos()
                .stream()
                .filter(r -> Objects.equals(r.orden(), orden))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una referencia con el orden: " + orden
                ));

        // Validar que la referencia exista en la Biblia
        bibliaService.validarReferencia(
                referencia.version(),
                referencia.libro(),
                referencia.capitulo(),
                referencia.versiculoDesde(),
                referencia.versiculoHasta()
        );

        // Obtener el capítulo de la Biblia
        JsonNode capitulo = bibliaService.obtenerCapitulo(
                referencia.version(),
                referencia.libro(),
                referencia.capitulo()
        );

        JsonNode versiculosBiblia = capitulo.path("Verses");

        // Preparar la respuesta
        Map<String, Object> resultado = new LinkedHashMap<>();

        resultado.put("temaId", tema.getId());
        resultado.put("tituloTema", tema.getTitulo());
        resultado.put("orden", referencia.orden());
        resultado.put("version", referencia.version());
        resultado.put("libro", referencia.libro());
        resultado.put("capitulo", referencia.capitulo());
        resultado.put("versiculoDesde", referencia.versiculoDesde());
        resultado.put("versiculoHasta", referencia.versiculoHasta());
        resultado.put("nota", referencia.nota());

        List<Map<String, Object>> versiculos = new ArrayList<>();

        // Extraer únicamente los versículos solicitados
        for (JsonNode versiculo : versiculosBiblia) {

                int numero = versiculo.path("Verse").asInt(-1);

                if (numero >= referencia.versiculoDesde()
                        && numero <= referencia.versiculoHasta()) {

                Map<String, Object> textoVersiculo =
                        new LinkedHashMap<>();

                textoVersiculo.put("numero", numero);
                textoVersiculo.put(
                        "texto",
                        versiculo.path("Text").asText()
                );

                versiculos.add(textoVersiculo);
                }
        }

        resultado.put("versiculos", versiculos);

        return resultado;
        }


        public TemaEstudio insertarVersiculo(
                String temaId,
                Integer posicion,
                ReferenciaVersiculo referencia,
                String usuarioId){

                if (posicion == null || posicion < 1) {
                        throw new IllegalArgumentException(
                                "La posición debe ser mayor o igual a 1."
                        );
                }

                if (referencia == null) {
                        throw new IllegalArgumentException(
                                "La referencia bíblica no puede ser nula."
                        );
                }

                // Validar que la referencia exista en la Biblia.
                validarReferencias(List.of(referencia));

                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                List<ReferenciaVersiculo> referencias =
                        new ArrayList<>(tema.getVersiculos());

                // Evitar referencias duplicadas.
                boolean duplicada = referencias.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(actual ->
                                Objects.equals(actual.version(), referencia.version())
                                && Objects.equals(actual.libro(), referencia.libro())
                                && Objects.equals(actual.capitulo(), referencia.capitulo())
                                && Objects.equals(
                                        actual.versiculoDesde(),
                                        referencia.versiculoDesde())
                                && Objects.equals(
                                        actual.versiculoHasta(),
                                        referencia.versiculoHasta()));

                if (duplicada) {
                        throw new IllegalArgumentException(
                                "La referencia ya existe en el tema."
                        );
                }

                // Se permite insertar desde la posición 1
                // hasta la siguiente posición disponible.
                if (posicion > referencias.size() + 1) {
                        throw new IllegalArgumentException(
                                "La posición no puede ser mayor que "
                                + (referencias.size() + 1) + "."
                        );
                }

                // Insertar la referencia en la posición indicada.
                referencias.add(
                        posicion - 1,
                        new ReferenciaVersiculo(
                                referencia.version(),
                                referencia.libro(),
                                referencia.capitulo(),
                                referencia.versiculoDesde(),
                                referencia.versiculoHasta(),
                                posicion,
                                referencia.nota()
                        )
                );

                // Renumerar las referencias para evitar órdenes repetidos.
                renumerarReferencias(referencias);

                tema.setVersiculos(referencias);
                tema.setFechaActualizacion(Instant.now());

                return repository.save(tema);
        }

        public List<ReferenciaVersiculo> listarVersiculos(String temaId, String usuarioId){
                TemaEstudio tema = obtenerPorIdDeUsuario(temaId, usuarioId);

                List<ReferenciaVersiculo> referencias =
                        new ArrayList<>(tema.getVersiculos());

                referencias.sort(
                        Comparator.comparing(
                                ReferenciaVersiculo::orden,
                                Comparator.nullsLast(Integer::compareTo)
                        )
                );

                return referencias;
        }

        public Page<TemaEstudio> listarPorUsuarioPaginado(
                String usuarioId,
                int page,
                int size) {

                if (usuarioId == null || usuarioId.isBlank()) {
                        throw new IllegalArgumentException(
                        "El usuarioId es obligatorio"
                        );
                }

                if (page < 0) {
                        throw new IllegalArgumentException(
                        "El número de página no puede ser negativo"
                        );
                }

                if (size < 1 || size > 100) {
                        throw new IllegalArgumentException(
                        "El tamaño de página debe estar entre 1 y 100"
                        );
                }

                Pageable pageable = PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "fechaActualizacion")
                );

                return repository
                        .findByUsuarioIdOrderByFechaActualizacionDesc(
                        usuarioId,
                        pageable
                        );
        }
}