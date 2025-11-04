package com.init.backend.controller;

import com.init.backend.dto.AuthResponse;
import com.init.backend.dto.FirebaseAuthRequest;
import com.init.backend.dto.LoginRequest;
import com.init.backend.dto.UsuarioDTO;
import com.init.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO createdUsuario = authService.register(usuarioDTO);
        return ResponseEntity.ok(createdUsuario);
    }
    
    @PostMapping("/firebase")
    public ResponseEntity<AuthResponse> loginWithFirebase(@Valid @RequestBody FirebaseAuthRequest firebaseRequest) {
        AuthResponse response = authService.loginWithFirebase(firebaseRequest);
        return ResponseEntity.ok(response);
    }
}
