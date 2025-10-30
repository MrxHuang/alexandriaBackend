package com.init.backend.controller;

import com.init.backend.dto.PrestamoDTO;
import com.init.backend.service.PrestamoService;
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
@RequestMapping("/prestamos")
public class PrestamoController {
    
    @Autowired
    private PrestamoService prestamoService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTOR')")
    public ResponseEntity<PrestamoDTO> createPrestamo(@Valid @RequestBody PrestamoDTO prestamoDTO) {
        PrestamoDTO createdPrestamo = prestamoService.createPrestamo(prestamoDTO);
        return new ResponseEntity<>(createdPrestamo, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PrestamoDTO> getPrestamoById(@PathVariable Long id) {
        PrestamoDTO prestamo = prestamoService.getPrestamoById(id);
        return ResponseEntity.ok(prestamo);
    }
    
    @GetMapping
    public ResponseEntity<?> getAllPrestamos(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long libroId,
            @RequestParam(required = false) Boolean devuelto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (usuarioId != null) {
            Page<PrestamoDTO> prestamos = prestamoService.getPrestamosByUsuario(usuarioId, pageable);
            return ResponseEntity.ok(prestamos);
        } else if (libroId != null) {
            Page<PrestamoDTO> prestamos = prestamoService.getPrestamosByLibro(libroId, pageable);
            return ResponseEntity.ok(prestamos);
        } else if (devuelto != null) {
            Page<PrestamoDTO> prestamos = prestamoService.getPrestamosByDevuelto(devuelto, pageable);
            return ResponseEntity.ok(prestamos);
        } else {
            Page<PrestamoDTO> prestamos = prestamoService.getAllPrestamosPaginated(pageable);
            return ResponseEntity.ok(prestamos);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<PrestamoDTO>> getAllPrestamosNoPagination() {
        List<PrestamoDTO> prestamos = prestamoService.getAllPrestamos();
        return ResponseEntity.ok(prestamos);
    }
    
    @PatchMapping("/{id}/devolver")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTOR')")
    public ResponseEntity<PrestamoDTO> devolverLibro(@PathVariable Long id) {
        PrestamoDTO prestamo = prestamoService.devolverLibro(id);
        return ResponseEntity.ok(prestamo);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrestamo(@PathVariable Long id) {
        prestamoService.deletePrestamo(id);
        return ResponseEntity.noContent().build();
    }
}