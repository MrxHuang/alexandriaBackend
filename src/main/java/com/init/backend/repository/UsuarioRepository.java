package com.init.backend.repository;

import com.init.backend.entity.Usuario;
import com.init.backend.entity.Usuario.UserRole;
import com.init.backend.entity.Usuario.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<Usuario> findByRol(UserRole rol);
    
    List<Usuario> findByEstado(UserStatus estado);
    
    Page<Usuario> findByRol(UserRole rol, Pageable pageable);
    
    Page<Usuario> findByEstado(UserStatus estado, Pageable pageable);
    
    Page<Usuario> findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String nombre, String username, String email, Pageable pageable);
}
