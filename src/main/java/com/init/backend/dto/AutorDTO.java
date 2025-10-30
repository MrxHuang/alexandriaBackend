package com.init.backend.dto;

import com.init.backend.entity.Autor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutorDTO {
    
    private Long id;
    
    @NotBlank(message = "Nombre is required")
    @Size(max = 100, message = "Nombre must be less than 100 characters")
    private String nombre;
    
    @NotBlank(message = "Apellido is required")
    @Size(max = 100, message = "Apellido must be less than 100 characters")
    private String apellido;
    
    @Size(max = 100, message = "Nacionalidad must be less than 100 characters")
    private String nacionalidad;
    
    @Past(message = "Fecha de nacimiento must be in the past")
    private LocalDate fechaNacimiento;
    
    public AutorDTO(Autor autor) {
        this.id = autor.getId();
        this.nombre = autor.getNombre();
        this.apellido = autor.getApellido();
        this.nacionalidad = autor.getNacionalidad();
        this.fechaNacimiento = autor.getFechaNacimiento();
    }
    
    public Autor toEntity() {
        Autor autor = new Autor();
        autor.setId(this.id);
        autor.setNombre(this.nombre);
        autor.setApellido(this.apellido);
        autor.setNacionalidad(this.nacionalidad);
        autor.setFechaNacimiento(this.fechaNacimiento);
        return autor;
    }
}
