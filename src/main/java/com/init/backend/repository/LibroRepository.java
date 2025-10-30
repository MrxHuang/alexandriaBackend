package com.init.backend.repository;

import com.init.backend.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    Page<Libro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
    
    Optional<Libro> findByIsbn(String isbn);
    
    List<Libro> findByAutorId(Long autorId);
    
    Page<Libro> findByAutorId(Long autorId, Pageable pageable);
    
    @Query("SELECT l FROM Libro l JOIN l.autor a WHERE " +
           "LOWER(l.titulo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(a.nombre, ' ', a.apellido)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Libro> searchLibros(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT l FROM Libro l JOIN l.autor a WHERE " +
           "(:titulo IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:autorNombre IS NULL OR LOWER(CONCAT(a.nombre, ' ', a.apellido)) LIKE LOWER(CONCAT('%', :autorNombre, '%'))) AND " +
           "(:isbn IS NULL OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', :isbn, '%')))")
    Page<Libro> searchWithFilters(
        @Param("titulo") String titulo,
        @Param("autorNombre") String autorNombre,
        @Param("isbn") String isbn,
        Pageable pageable);
    
    boolean existsByIsbn(String isbn);
}
