package com.init.backend.dto;

import com.init.backend.entity.Prestamo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoDTO {
    
    private Long id;
    
    @NotNull(message = "Libro ID is required")
    private Long libroId;
    
    @NotNull(message = "Usuario ID is required")
    private Long usuarioId;
    
    private LocalDate fechaPrestamo;
    
    private LocalDate fechaDevolucion;
    
    private Boolean devuelto;
    
    private LibroDTO libro;
    
    private UsuarioDTO usuario;
    
    public PrestamoDTO(Prestamo prestamo) {
        this.id = prestamo.getId();
        this.libroId = prestamo.getLibro().getId();
        this.usuarioId = prestamo.getUsuario().getId();
        this.fechaPrestamo = prestamo.getFechaPrestamo();
        this.fechaDevolucion = prestamo.getFechaDevolucion();
        this.devuelto = prestamo.getDevuelto();
        this.libro = new LibroDTO(prestamo.getLibro());
        this.usuario = new UsuarioDTO(prestamo.getUsuario());
    }
    
    public PrestamoDTO(Prestamo prestamo, boolean includeRelations) {
        this.id = prestamo.getId();
        this.libroId = prestamo.getLibro().getId();
        this.usuarioId = prestamo.getUsuario().getId();
        this.fechaPrestamo = prestamo.getFechaPrestamo();
        this.fechaDevolucion = prestamo.getFechaDevolucion();
        this.devuelto = prestamo.getDevuelto();
        if (includeRelations) {
            this.libro = new LibroDTO(prestamo.getLibro());
            this.usuario = new UsuarioDTO(prestamo.getUsuario());
        }
    }
}
