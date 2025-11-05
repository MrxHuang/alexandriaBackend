package com.init.backend.repository;

import com.init.backend.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    Page<Libro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
    
    Optional<Libro> findByIsbn(String isbn);
    
    List<Libro> findByAutorId(Long autorId);
    
    Page<Libro> findByAutorId(Long autorId, Pageable pageable);
    
    Page<Libro> findByTituloContainingIgnoreCaseOrAutorNombreContainingIgnoreCaseOrAutorApellidoContainingIgnoreCaseOrIsbnContainingIgnoreCase(
        String searchTerm, String searchTerm2, String searchTerm3, String searchTerm4, Pageable pageable);
    
    Page<Libro> findByIsbnContainingIgnoreCase(String isbn, Pageable pageable);
    
    Page<Libro> findByAutorNombreContainingIgnoreCaseOrAutorApellidoContainingIgnoreCase(
        String autorNombre, String autorApellido, Pageable pageable);
    
    Page<Libro> findByTituloContainingIgnoreCaseAndAutorNombreContainingIgnoreCase(
        String titulo, String autorNombre, Pageable pageable);
    
    Page<Libro> findByTituloContainingIgnoreCaseAndAutorApellidoContainingIgnoreCase(
        String titulo, String autorApellido, Pageable pageable);
    
    Page<Libro> findByTituloContainingIgnoreCaseAndIsbnContainingIgnoreCase(
        String titulo, String isbn, Pageable pageable);
    
    Page<Libro> findByAutorNombreContainingIgnoreCaseAndIsbnContainingIgnoreCase(
        String autorNombre, String isbn, Pageable pageable);
    
    Page<Libro> findByAutorApellidoContainingIgnoreCaseAndIsbnContainingIgnoreCase(
        String autorApellido, String isbn, Pageable pageable);
    
    Page<Libro> findByTituloContainingIgnoreCaseAndAutorNombreContainingIgnoreCaseAndIsbnContainingIgnoreCase(
        String titulo, String autorNombre, String isbn, Pageable pageable);
    
    Page<Libro> findByTituloContainingIgnoreCaseAndAutorApellidoContainingIgnoreCaseAndIsbnContainingIgnoreCase(
        String titulo, String autorApellido, String isbn, Pageable pageable);
    
    boolean existsByIsbn(String isbn);
}
