package com.init.backend.dto;

import com.init.backend.entity.Usuario;
import com.init.backend.entity.Usuario.UserRole;
import com.init.backend.entity.Usuario.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    
    private Long id;
    
    @NotBlank(message = "Nombre is required")
    @Size(max = 100, message = "Nombre must be less than 100 characters")
    private String nombre;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @NotNull(message = "Rol is required")
    private UserRole rol;
    
    @NotNull(message = "Estado is required")
    private UserStatus estado;
    
    // Password only for creation/update, not returned
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.rol = usuario.getRol();
        this.estado = usuario.getEstado();
        // Password is never returned
    }
    
    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setId(this.id);
        usuario.setNombre(this.nombre);
        usuario.setUsername(this.username);
        usuario.setEmail(this.email);
        usuario.setRol(this.rol);
        usuario.setEstado(this.estado);
        return usuario;
    }
}
