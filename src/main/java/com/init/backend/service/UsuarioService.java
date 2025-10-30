package com.init.backend.service;

import com.init.backend.dto.UsuarioDTO;
import com.init.backend.entity.Usuario;
import com.init.backend.exception.ResourceNotFoundException;
import com.init.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public UsuarioDTO createUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Usuario usuario = usuarioDTO.toEntity();
        usuario.setPasswordHash(passwordEncoder.encode(usuarioDTO.getPassword()));
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return new UsuarioDTO(savedUsuario);
    }
    
    public UsuarioDTO getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return new UsuarioDTO(usuario);
    }
    
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioDTO::new)
                .collect(Collectors.toList());
    }
    
    public Page<UsuarioDTO> getAllUsuariosPaginated(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(UsuarioDTO::new);
    }
    
    public Page<UsuarioDTO> searchUsuarios(String searchTerm, Pageable pageable) {
        return usuarioRepository.findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, pageable)
                .map(UsuarioDTO::new);
    }
    
    public Page<UsuarioDTO> getUsuariosByRol(Usuario.UserRole rol, Pageable pageable) {
        return usuarioRepository.findByRol(rol, pageable)
                .map(UsuarioDTO::new);
    }
    
    public UsuarioDTO updateUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        if (usuarioDTO.getNombre() != null) {
            usuario.setNombre(usuarioDTO.getNombre());
        }
        if (usuarioDTO.getUsername() != null && !usuarioDTO.getUsername().equals(usuario.getUsername())) {
            if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            usuario.setUsername(usuarioDTO.getUsername());
        }
        if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            usuario.setEmail(usuarioDTO.getEmail());
        }
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
            usuario.setPasswordHash(passwordEncoder.encode(usuarioDTO.getPassword()));
        }
        if (usuarioDTO.getRol() != null) {
            usuario.setRol(usuarioDTO.getRol());
        }
        if (usuarioDTO.getEstado() != null) {
            usuario.setEstado(usuarioDTO.getEstado());
        }
        
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return new UsuarioDTO(updatedUsuario);
    }
    
    public void deleteUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
    }
}
