package com.init.backend.service;

import com.init.backend.dto.PrestamoDTO;
import com.init.backend.entity.Libro;
import com.init.backend.entity.Prestamo;
import com.init.backend.entity.Usuario;
import com.init.backend.exception.ResourceNotFoundException;
import com.init.backend.repository.LibroRepository;
import com.init.backend.repository.PrestamoRepository;
import com.init.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrestamoService {
    
    @Autowired
    private PrestamoRepository prestamoRepository;
    
    @Autowired
    private LibroRepository libroRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @CacheEvict(value = {"prestamos", "libros", "librosByAutor"}, allEntries = true)
    public PrestamoDTO createPrestamo(PrestamoDTO prestamoDTO) {
        Libro libro = libroRepository.findById(prestamoDTO.getLibroId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", prestamoDTO.getLibroId()));
        
        Usuario usuario = usuarioRepository.findById(prestamoDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", prestamoDTO.getUsuarioId()));
        
        // Check if book is available
        List<Prestamo> activePrestamos = prestamoRepository.findByLibroIdAndDevueltoFalse(libro.getId());
        if (!activePrestamos.isEmpty()) {
            throw new IllegalArgumentException("This book is currently on loan");
        }
        
        // Check user loan limit (max 3 active loans)
        Long activeLoans = prestamoRepository.countByUsuarioIdAndDevueltoFalse(usuario.getId());
        if (activeLoans >= 3) {
            throw new IllegalArgumentException("User has reached the maximum number of active loans (3)");
        }
        
        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setUsuario(usuario);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setDevuelto(false);
        
        Prestamo savedPrestamo = prestamoRepository.save(prestamo);
        return new PrestamoDTO(savedPrestamo);
    }
    
    @Cacheable(value = "prestamos", key = "#id")
    public PrestamoDTO getPrestamoById(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo", "id", id));
        return new PrestamoDTO(prestamo);
    }
    
    public List<PrestamoDTO> getAllPrestamos() {
        return prestamoRepository.findAll().stream()
                .map(PrestamoDTO::new)
                .collect(Collectors.toList());
    }
    
    public Page<PrestamoDTO> getAllPrestamosPaginated(Pageable pageable) {
        return prestamoRepository.findAll(pageable)
                .map(PrestamoDTO::new);
    }
    
    public Page<PrestamoDTO> getPrestamosByUsuario(Long usuarioId, Pageable pageable) {
        return prestamoRepository.findByUsuarioId(usuarioId, pageable)
                .map(PrestamoDTO::new);
    }
    
    public Page<PrestamoDTO> getPrestamosByLibro(Long libroId, Pageable pageable) {
        return prestamoRepository.findByLibroId(libroId, pageable)
                .map(PrestamoDTO::new);
    }
    
    public Page<PrestamoDTO> getPrestamosByDevuelto(Boolean devuelto, Pageable pageable) {
        return prestamoRepository.findByDevuelto(devuelto, pageable)
                .map(PrestamoDTO::new);
    }
    
    @CacheEvict(value = {"prestamos", "libros", "librosByAutor"}, key = "#id", allEntries = true)
    public PrestamoDTO devolverLibro(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo", "id", id));
        
        if (prestamo.getDevuelto()) {
            throw new IllegalArgumentException("This book has already been returned");
        }
        
        prestamo.setDevuelto(true);
        prestamo.setFechaDevolucion(LocalDate.now());
        
        Prestamo updatedPrestamo = prestamoRepository.save(prestamo);
        return new PrestamoDTO(updatedPrestamo);
    }
    
    @CacheEvict(value = {"prestamos", "libros", "librosByAutor"}, key = "#id", allEntries = true)
    public void deletePrestamo(Long id) {
        if (!prestamoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prestamo", "id", id);
        }
        prestamoRepository.deleteById(id);
    }
}
