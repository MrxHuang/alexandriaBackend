package com.init.backend.repository;

import com.init.backend.entity.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    Page<Autor> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
        String nombre, String apellido, Pageable pageable);
    
    List<Autor> findByNacionalidad(String nacionalidad);
    
    boolean existsByNombreAndApellido(String nombre, String apellido);
}
