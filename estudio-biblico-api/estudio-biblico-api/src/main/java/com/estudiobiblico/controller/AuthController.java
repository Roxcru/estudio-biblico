package com.estudiobiblico.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.estudiobiblico.dto.LoginRequest;
import com.estudiobiblico.dto.LoginResponse;
import com.estudiobiblico.dto.RegistroUsuarioRequest;
import com.estudiobiblico.dto.UsuarioResponse;
import com.estudiobiblico.model.Usuario;
import com.estudiobiblico.service.JwtService;
import com.estudiobiblico.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(
            UsuarioService usuarioService,
            JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroUsuarioRequest request) {

        Usuario usuario = usuarioService.registrar(
                request.nombre(),
                request.correo(),
                request.contrasena()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UsuarioResponse.desde(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        Usuario usuario = usuarioService.autenticar(
                request.correo(),
                request.contrasena()
        );

        String token = jwtService.generarToken(usuario);

        LoginResponse respuesta = new LoginResponse(
                token,
                "Bearer",
                3600,
                UsuarioResponse.desde(usuario)
        );

        return ResponseEntity.ok(respuesta);
    }
}
