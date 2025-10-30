package com.init.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@Entity
@Table(name = "prestamo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prestamo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    @JsonIgnoreProperties({"prestamos", "hibernateLazyInitializer", "handler"})
    private Libro libro;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"prestamos", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;
    
    @Column(nullable = false, name = "fecha_prestamo")
    private LocalDate fechaPrestamo;
    
    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;
    
    @Column(nullable = false)
    private Boolean devuelto = false;
}
