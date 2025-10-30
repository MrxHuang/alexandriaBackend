package com.init.backend.controller;

import com.init.backend.dto.AutorDTO;
import com.init.backend.service.AutorService;
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
@RequestMapping("/autores")
public class AutorController {
    
    @Autowired
    private AutorService autorService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AutorDTO> createAutor(@Valid @RequestBody AutorDTO autorDTO) {
        AutorDTO createdAutor = autorService.createAutor(autorDTO);
        return new ResponseEntity<>(createdAutor, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AutorDTO> getAutorById(@PathVariable Long id) {
        AutorDTO autor = autorService.getAutorById(id);
        return ResponseEntity.ok(autor);
    }
    
    @GetMapping
    public ResponseEntity<?> getAllAutores(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (search != null && !search.isEmpty()) {
            Page<AutorDTO> autores = autorService.searchAutores(search, pageable);
            return ResponseEntity.ok(autores);
        } else {
            Page<AutorDTO> autores = autorService.getAllAutoresPaginated(pageable);
            return ResponseEntity.ok(autores);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<AutorDTO>> getAllAutoresNoPagination() {
        List<AutorDTO> autores = autorService.getAllAutores();
        return ResponseEntity.ok(autores);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AutorDTO> updateAutor(
            @PathVariable Long id,
            @Valid @RequestBody AutorDTO autorDTO) {
        AutorDTO updatedAutor = autorService.updateAutor(id, autorDTO);
        return ResponseEntity.ok(updatedAutor);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAutor(@PathVariable Long id) {
        autorService.deleteAutor(id);
        return ResponseEntity.noContent().build();
    }
}
