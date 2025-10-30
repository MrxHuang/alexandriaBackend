package com.init.backend.service;

import com.init.backend.dto.AuthResponse;
import com.init.backend.dto.LoginRequest;
import com.init.backend.dto.UsuarioDTO;
import com.init.backend.entity.Usuario;
import com.init.backend.repository.UsuarioRepository;
import com.init.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse login(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        if (usuario.getEstado() == Usuario.UserStatus.INACTIVO) {
            throw new IllegalArgumentException("User account is inactive");
        }
        
        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name());
        return new AuthResponse(token, usuario);
    }
    
    public UsuarioDTO register(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Usuario usuario = usuarioDTO.toEntity();
        usuario.setPasswordHash(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setEstado(Usuario.UserStatus.ACTIVO);
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return new UsuarioDTO(savedUsuario);
    }
}
