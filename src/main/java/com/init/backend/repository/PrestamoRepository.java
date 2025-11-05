package com.init.backend.repository;

import com.init.backend.entity.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    List<Prestamo> findByUsuarioId(Long usuarioId);
    
    Page<Prestamo> findByUsuarioId(Long usuarioId, Pageable pageable);
    
    List<Prestamo> findByLibroId(Long libroId);
    
    Page<Prestamo> findByLibroId(Long libroId, Pageable pageable);
    
    List<Prestamo> findByDevuelto(Boolean devuelto);
    
    Page<Prestamo> findByDevuelto(Boolean devuelto, Pageable pageable);
    
    List<Prestamo> findByUsuarioIdAndDevuelto(Long usuarioId, Boolean devuelto);
    
    Page<Prestamo> findByUsuarioIdAndDevuelto(Long usuarioId, Boolean devuelto, Pageable pageable);
    
    List<Prestamo> findByLibroIdAndDevueltoFalse(Long libroId);
    
    Long countByUsuarioIdAndDevueltoFalse(Long usuarioId);
}
