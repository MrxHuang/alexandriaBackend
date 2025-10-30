package com.init.backend.dto;

import com.init.backend.entity.Libro;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibroDTO {
    
    private Long id;
    
    @NotBlank(message = "Titulo is required")
    @Size(max = 200, message = "Titulo must be less than 200 characters")
    private String titulo;
    
    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN must be less than 20 characters")
    private String isbn;
    
    @NotNull(message = "Año is required")
    @Min(value = 1000, message = "Año must be greater than 1000")
    @Max(value = 2100, message = "Año must be less than 2100")
    private Integer anio;
    
    @NotNull(message = "Autor ID is required")
    private Long autorId;
    
    private AutorDTO autor;
    
    public LibroDTO(Libro libro) {
        this.id = libro.getId();
        this.titulo = libro.getTitulo();
        this.isbn = libro.getIsbn();
        this.anio = libro.getAnio();
        this.autorId = libro.getAutor().getId();
        this.autor = new AutorDTO(libro.getAutor());
    }
    
    public LibroDTO(Libro libro, boolean includeAutor) {
        this.id = libro.getId();
        this.titulo = libro.getTitulo();
        this.isbn = libro.getIsbn();
        this.anio = libro.getAnio();
        this.autorId = libro.getAutor().getId();
        if (includeAutor) {
            this.autor = new AutorDTO(libro.getAutor());
        }
    }
}
