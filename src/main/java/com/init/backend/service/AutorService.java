package com.init.backend.service;

import com.init.backend.dto.AutorDTO;
import com.init.backend.entity.Autor;
import com.init.backend.exception.ResourceNotFoundException;
import com.init.backend.repository.AutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AutorService {
    
    @Autowired
    private AutorRepository autorRepository;
    
    public AutorDTO createAutor(AutorDTO autorDTO) {
        if (autorRepository.existsByNombreAndApellido(autorDTO.getNombre(), autorDTO.getApellido())) {
            throw new IllegalArgumentException("Autor with this name already exists");
        }
        
        Autor autor = autorDTO.toEntity();
        Autor savedAutor = autorRepository.save(autor);
        return new AutorDTO(savedAutor);
    }
    
    public AutorDTO getAutorById(Long id) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", id));
        return new AutorDTO(autor);
    }
    
    public List<AutorDTO> getAllAutores() {
        return autorRepository.findAll().stream()
                .map(AutorDTO::new)
                .collect(Collectors.toList());
    }
    
    public Page<AutorDTO> getAllAutoresPaginated(Pageable pageable) {
        return autorRepository.findAll(pageable)
                .map(AutorDTO::new);
    }
    
    public Page<AutorDTO> searchAutores(String searchTerm, Pageable pageable) {
        return autorRepository.searchByFullName(searchTerm, pageable)
                .map(AutorDTO::new);
    }
    
    public AutorDTO updateAutor(Long id, AutorDTO autorDTO) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", id));
        
        if (autorDTO.getNombre() != null) {
            autor.setNombre(autorDTO.getNombre());
        }
        if (autorDTO.getApellido() != null) {
            autor.setApellido(autorDTO.getApellido());
        }
        if (autorDTO.getNacionalidad() != null) {
            autor.setNacionalidad(autorDTO.getNacionalidad());
        }
        if (autorDTO.getFechaNacimiento() != null) {
            autor.setFechaNacimiento(autorDTO.getFechaNacimiento());
        }
        
        Autor updatedAutor = autorRepository.save(autor);
        return new AutorDTO(updatedAutor);
    }
    
    public void deleteAutor(Long id) {
        if (!autorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Autor", "id", id);
        }
        autorRepository.deleteById(id);
    }
}
