package com.estudiobiblico.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Document(collection = "temas_estudio")
public class TemaEstudio {

    @Id
    private String id;

    @NotBlank(message = "El usuarioId es obligatorio")
    private String usuarioId;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;
    private String introduccion;
    private String conclusion;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
        regexp = "BORRADOR|PUBLICADO|ARCHIVADO",
        message = "El estado debe ser BORRADOR, PUBLICADO o ARCHIVADO"
    )
    private String estado = "BORRADOR";
    private boolean esPublico = false;

    @Valid
    private List<ReferenciaVersiculo> versiculos = new ArrayList<>();

    private Instant fechaCreacion = Instant.now();
    private Instant fechaActualizacion = Instant.now();

    public TemaEstudio() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIntroduccion() { return introduccion; }
    public void setIntroduccion(String introduccion) {
        this.introduccion = introduccion;
    }

    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isEsPublico() { return esPublico; }
    public void setEsPublico(boolean esPublico) {
        this.esPublico = esPublico;
    }

    public List<ReferenciaVersiculo> getVersiculos() {
        return versiculos;
    }

    public void setVersiculos(List<ReferenciaVersiculo> versiculos) {
        this.versiculos = versiculos != null
            ? new ArrayList<>(versiculos)
            : new ArrayList<>();
    }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Instant fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}