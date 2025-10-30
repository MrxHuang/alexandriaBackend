package com.init.backend.service;

import com.init.backend.dto.LibroDTO;
import com.init.backend.entity.Autor;
import com.init.backend.entity.Libro;
import com.init.backend.exception.ResourceNotFoundException;
import com.init.backend.repository.AutorRepository;
import com.init.backend.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        return libroRepository.searchLibros(searchTerm, pageable)
                .map(LibroDTO::new);
    }
    
    public Page<LibroDTO> searchLibrosWithFilters(String titulo, String autorNombre, String isbn, Pageable pageable) {
        return libroRepository.searchWithFilters(titulo, autorNombre, isbn, pageable)
                .map(LibroDTO::new);
    }
    
    public List<LibroDTO> getLibrosByAutor(Long autorId) {
        return libroRepository.findByAutorId(autorId).stream()
                .map(LibroDTO::new)
                .collect(Collectors.toList());
    }
    
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
    
    public void deleteLibro(Long id) {
        if (!libroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Libro", "id", id);
        }
        libroRepository.deleteById(id);
    }
}
