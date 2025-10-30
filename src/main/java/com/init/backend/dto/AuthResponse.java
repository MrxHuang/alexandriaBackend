package com.init.backend.dto;

import com.init.backend.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private UsuarioDTO usuario;
    
    public AuthResponse(String token, Usuario usuario) {
        this.token = token;
        this.usuario = new UsuarioDTO(usuario);
    }
}
