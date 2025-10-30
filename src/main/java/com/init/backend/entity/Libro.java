package com.init.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Entity
@Table(name = "libro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Libro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(unique = true, nullable = false, length = 20)
    private String isbn;
    
    @Column(nullable = false)
    private Integer anio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    @JsonIgnoreProperties({"libros", "hibernateLazyInitializer", "handler"})
    private Autor autor;
    
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("libro")
    private List<Prestamo> prestamos;
}
