package com.init.backend.service;

import com.init.backend.dto.LibroDTO;
import com.init.backend.entity.Autor;
import com.init.backend.entity.Libro;
import com.init.backend.exception.ResourceNotFoundException;
import com.init.backend.repository.AutorRepository;
import com.init.backend.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LibroService {
    
    @Autowired
    private LibroRepository libroRepository;
    
    @Autowired
    private AutorRepository autorRepository;
    
    @CacheEvict(value = {"libros", "librosByAutor"}, allEntries = true)
    public LibroDTO createLibro(LibroDTO libroDTO) {
        if (libroRepository.existsByIsbn(libroDTO.getIsbn())) {
            throw new IllegalArgumentException("Libro with this ISBN already exists");
        }
        
        Autor autor = autorRepository.findById(libroDTO.getAutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", libroDTO.getAutorId()));
        
        Libro libro = new Libro();
        libro.setTitulo(libroDTO.getTitulo());
        libro.setIsbn(libroDTO.getIsbn());
        libro.setAnio(libroDTO.getAnio());
        libro.setAutor(autor);
        
        Libro savedLibro = libroRepository.save(libro);
        return new LibroDTO(savedLibro);
    }
    
    @Cacheable(value = "libros", key = "#id")
    public LibroDTO getLibroById(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));
        return new LibroDTO(libro);
    }
    
    public List<LibroDTO> getAllLibros() {
        return libroRepository.findAll().stream()
                .map(LibroDTO::new)
                .collect(Collectors.toList());
    }
    
    public Page<LibroDTO> getAllLibrosPaginated(Pageable pageable) {
        return libroRepository.findAll(pageable)
                .map(LibroDTO::new);
    }
    
    public Page<LibroDTO> searchLibros(String searchTerm, Pageable pageable) {
        return libroRepository.findByTituloContainingIgnoreCaseOrAutorNombreContainingIgnoreCaseOrAutorApellidoContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, searchTerm, pageable)
                .map(LibroDTO::new);
    }
    
    public Page<LibroDTO> searchLibrosWithFilters(String titulo, String autorNombre, String isbn, Pageable pageable) {
        boolean hasTitulo = titulo != null && !titulo.isEmpty();
        boolean hasAutor = autorNombre != null && !autorNombre.isEmpty();
        boolean hasIsbn = isbn != null && !isbn.isEmpty();
        
        // Si todos los filtros están presentes
        if (hasTitulo && hasAutor && hasIsbn) {
            // Intentar primero con nombre, luego con apellido si no hay resultados
            Page<Libro> result = libroRepository.findByTituloContainingIgnoreCaseAndAutorNombreContainingIgnoreCaseAndIsbnContainingIgnoreCase(
                    titulo, autorNombre, isbn, pageable);
            if (result.isEmpty()) {
                result = libroRepository.findByTituloContainingIgnoreCaseAndAutorApellidoContainingIgnoreCaseAndIsbnContainingIgnoreCase(
                        titulo, autorNombre, isbn, pageable);
            }
            return result.map(LibroDTO::new);
        }
        
        // Si solo título y autor
        if (hasTitulo && hasAutor && !hasIsbn) {
            Page<Libro> byNombre = libroRepository.findByTituloContainingIgnoreCaseAndAutorNombreContainingIgnoreCase(
                    titulo, autorNombre, pageable);
            Page<Libro> byApellido = libroRepository.findByTituloContainingIgnoreCaseAndAutorApellidoContainingIgnoreCase(
                    titulo, autorNombre, pageable);
            // Usar el que tenga más resultados o el primero
            return (byNombre.getTotalElements() > byApellido.getTotalElements() ? byNombre : byApellido)
                    .map(LibroDTO::new);
        }
        
        // Si solo título y ISBN
        if (hasTitulo && !hasAutor && hasIsbn) {
            return libroRepository.findByTituloContainingIgnoreCaseAndIsbnContainingIgnoreCase(
                    titulo, isbn, pageable)
                    .map(LibroDTO::new);
        }
        
        // Si solo autor y ISBN
        if (!hasTitulo && hasAutor && hasIsbn) {
            Page<Libro> byNombre = libroRepository.findByAutorNombreContainingIgnoreCaseAndIsbnContainingIgnoreCase(
                    autorNombre, isbn, pageable);
            Page<Libro> byApellido = libroRepository.findByAutorApellidoContainingIgnoreCaseAndIsbnContainingIgnoreCase(
                    autorNombre, isbn, pageable);
            return (byNombre.getTotalElements() > byApellido.getTotalElements() ? byNombre : byApellido)
                    .map(LibroDTO::new);
        }
        
        // Si solo título
        if (hasTitulo && !hasAutor && !hasIsbn) {
            return libroRepository.findByTituloContainingIgnoreCase(titulo, pageable)
                    .map(LibroDTO::new);
        }
        
        // Si solo autor
        if (!hasTitulo && hasAutor && !hasIsbn) {
            return libroRepository.findByAutorNombreContainingIgnoreCaseOrAutorApellidoContainingIgnoreCase(
                    autorNombre, autorNombre, pageable)
                    .map(LibroDTO::new);
        }
        
        // Si solo ISBN
        if (!hasTitulo && !hasAutor && hasIsbn) {
            return libroRepository.findByIsbnContainingIgnoreCase(isbn, pageable)
                    .map(LibroDTO::new);
        }
        
        // Si no hay filtros, devolver todos
        return getAllLibrosPaginated(pageable);
    }
    
    @Cacheable(value = "librosByAutor", key = "#autorId")
    public List<LibroDTO> getLibrosByAutor(Long autorId) {
        return libroRepository.findByAutorId(autorId).stream()
                .map(LibroDTO::new)
                .collect(Collectors.toList());
    }
    
    @CacheEvict(value = {"libros", "librosByAutor"}, key = "#id", allEntries = true)
    public LibroDTO updateLibro(Long id, LibroDTO libroDTO) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));
        
        if (libroDTO.getTitulo() != null) {
            libro.setTitulo(libroDTO.getTitulo());
        }
        if (libroDTO.getIsbn() != null && !libroDTO.getIsbn().equals(libro.getIsbn())) {
            if (libroRepository.existsByIsbn(libroDTO.getIsbn())) {
                throw new IllegalArgumentException("ISBN already exists");
            }
            libro.setIsbn(libroDTO.getIsbn());
        }
        if (libroDTO.getAnio() != null) {
            libro.setAnio(libroDTO.getAnio());
        }
        if (libroDTO.getAutorId() != null && !libroDTO.getAutorId().equals(libro.getAutor().getId())) {
            Autor autor = autorRepository.findById(libroDTO.getAutorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", libroDTO.getAutorId()));
            libro.setAutor(autor);
        }
        
        Libro updatedLibro = libroRepository.save(libro);
        return new LibroDTO(updatedLibro);
    }
    
    @CacheEvict(value = {"libros", "librosByAutor"}, key = "#id", allEntries = true)
    public void deleteLibro(Long id) {
        if (!libroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Libro", "id", id);
        }
        libroRepository.deleteById(id);
    }
}
