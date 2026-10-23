package com.estudiobiblico.service;

import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.estudiobiblico.model.Usuario;
import com.estudiobiblico.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(
            String nombre,
            String correo,
            String contrasena) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio");
        }

        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo es obligatorio");
        }

        if (contrasena == null || contrasena.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres");
        }

        String nombreNormalizado = nombre.trim();
        String correoNormalizado = correo.trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreoIgnoreCase(
                correoNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombreNormalizado);
        usuario.setCorreo(correoNormalizado);
        usuario.setPasswordHash(
                passwordEncoder.encode(contrasena));
        usuario.setActivo(true);
        usuario.setFechaCreacion(Instant.now());

        return usuarioRepository.save(usuario);
    }

    public Usuario autenticar(String correo, String contrasena) {
        if (correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            throw new BadCredentialsException(
                    "Correo o contraseña incorrectos");
        }

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new BadCredentialsException(
                        "Correo o contraseña incorrectos"));

        if (!usuario.isActivo()
                || !passwordEncoder.matches(
                        contrasena, usuario.getPasswordHash())) {
            throw new BadCredentialsException(
                    "Correo o contraseña incorrectos");
        }

        return usuario;
    }
}
