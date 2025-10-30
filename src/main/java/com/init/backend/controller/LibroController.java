package com.init.backend.controller;

import com.init.backend.dto.LibroDTO;
import com.init.backend.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
public class LibroController {
    
    @Autowired
    private LibroService libroService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LibroDTO> createLibro(@Valid @RequestBody LibroDTO libroDTO) {
        LibroDTO createdLibro = libroService.createLibro(libroDTO);
        return new ResponseEntity<>(createdLibro, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LibroDTO> getLibroById(@PathVariable Long id) {
        LibroDTO libro = libroService.getLibroById(id);
        return ResponseEntity.ok(libro);
    }
    
    @GetMapping
    public ResponseEntity<?> getAllLibros(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String isbn,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (search != null && !search.isEmpty()) {
            Page<LibroDTO> libros = libroService.searchLibros(search, pageable);
            return ResponseEntity.ok(libros);
        } else if (titulo != null || autor != null || isbn != null) {
            Page<LibroDTO> libros = libroService.searchLibrosWithFilters(titulo, autor, isbn, pageable);
            return ResponseEntity.ok(libros);
        } else {
            Page<LibroDTO> libros = libroService.getAllLibrosPaginated(pageable);
            return ResponseEntity.ok(libros);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<LibroDTO>> getAllLibrosNoPagination() {
        List<LibroDTO> libros = libroService.getAllLibros();
        return ResponseEntity.ok(libros);
    }
    
    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<LibroDTO>> getLibrosByAutor(@PathVariable Long autorId) {
        List<LibroDTO> libros = libroService.getLibrosByAutor(autorId);
        return ResponseEntity.ok(libros);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LibroDTO> updateLibro(
            @PathVariable Long id,
            @Valid @RequestBody LibroDTO libroDTO) {
        LibroDTO updatedLibro = libroService.updateLibro(id, libroDTO);
        return ResponseEntity.ok(updatedLibro);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLibro(@PathVariable Long id) {
        libroService.deleteLibro(id);
        return ResponseEntity.noContent().build();
    }
}
