package com.init.backend.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.init.backend.dto.AuthResponse;
import com.init.backend.dto.FirebaseAuthRequest;
import com.init.backend.dto.LoginRequest;
import com.init.backend.dto.UsuarioDTO;
import com.init.backend.entity.Usuario;
import com.init.backend.repository.UsuarioRepository;
import com.init.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private FirebaseService firebaseService;
    
    public AuthResponse login(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        if (usuario.getEstado() == Usuario.UserStatus.INACTIVO) {
            throw new IllegalArgumentException("User account is inactive");
        }
        
        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name());
        return new AuthResponse(token, usuario);
    }
    
    public UsuarioDTO register(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Usuario usuario = usuarioDTO.toEntity();
        usuario.setPasswordHash(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setEstado(Usuario.UserStatus.ACTIVO);
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return new UsuarioDTO(savedUsuario);
    }
    
    /**
     * Autentica un usuario con Firebase OAuth
     */
    public AuthResponse loginWithFirebase(FirebaseAuthRequest firebaseRequest) {
        try {
            // Verificar token de Firebase
            FirebaseToken decodedToken = firebaseService.verifyToken(firebaseRequest.getIdToken());
            
            String email = decodedToken.getEmail();
            String name = decodedToken.getName() != null ? decodedToken.getName() : firebaseRequest.getName();
            
            // Buscar usuario por email o crear uno nuevo
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElse(null);
            
            if (usuario == null) {
                // Crear nuevo usuario desde Firebase
                logger.info("Creando nuevo usuario OAuth - Email: {}, Provider: {}", email, firebaseRequest.getProvider());
                usuario = new Usuario();
                usuario.setNombre(name != null ? name : email.split("@")[0]);
                usuario.setUsername(generateUniqueUsername(email));
                usuario.setEmail(email);
                // Para usuarios OAuth, generamos un password hash aleatorio (no se usará)
                usuario.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                
                // Determinar el rol: usar el rol proporcionado o el determinado por reglas
                Usuario.UserRole selectedRole;
                if (firebaseRequest.getRole() != null && !firebaseRequest.getRole().isEmpty()) {
                    try {
                        selectedRole = Usuario.UserRole.valueOf(firebaseRequest.getRole().toUpperCase());
                        logger.info("Rol proporcionado desde frontend: {}", selectedRole);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Rol inválido proporcionado: {}, usando reglas por defecto", firebaseRequest.getRole());
                        selectedRole = determineUserRole(email);
                    }
                } else {
                    selectedRole = determineUserRole(email);
                }
                usuario.setRol(selectedRole);
                usuario.setEstado(Usuario.UserStatus.ACTIVO);
                
                usuario = usuarioRepository.save(usuario);
                logger.info("Usuario OAuth guardado en base de datos - ID: {}, Username: {}, Email: {}, Rol: {}", 
                        usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getRol());
            } else {
                // Usuario ya existe - validar que no se intente cambiar el rol
                logger.info("Usuario OAuth existente encontrado - ID: {}, Username: {}, Email: {}, Rol actual: {}", 
                        usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getRol());
                
                // Validar intento de cambio de rol
                if (firebaseRequest.getRole() != null && !firebaseRequest.getRole().isEmpty()) {
                    try {
                        Usuario.UserRole requestedRole = Usuario.UserRole.valueOf(firebaseRequest.getRole().toUpperCase());
                        if (requestedRole != usuario.getRol()) {
                            logger.warn("Intento de cambio de rol rechazado - Usuario: {} (ID: {}), Rol actual: {}, Rol solicitado: {}", 
                                    usuario.getEmail(), usuario.getId(), usuario.getRol(), requestedRole);
                            throw new IllegalArgumentException(
                                String.format("No se puede cambiar el rol de un usuario existente. Tu cuenta (%s) ya está registrada como %s. " +
                                "Por favor, inicia sesión con tu cuenta existente o contacta a un administrador para cambiar tu rol.", 
                                usuario.getEmail(), usuario.getRol().name())
                            );
                        }
                    } catch (IllegalArgumentException e) {
                        // Si es nuestro error de cambio de rol, relanzarlo
                        if (e.getMessage().contains("No se puede cambiar el rol")) {
                            throw e;
                        }
                        // Si es error de parsing, ignorarlo
                        logger.debug("Rol proporcionado no válido, ignorando: {}", firebaseRequest.getRole());
                    }
                }
                
                // Si el usuario existe pero está inactivo, activarlo
                if (usuario.getEstado() == Usuario.UserStatus.INACTIVO) {
                    usuario.setEstado(Usuario.UserStatus.ACTIVO);
                    usuario = usuarioRepository.save(usuario);
                    logger.info("Usuario OAuth reactivado - ID: {}", usuario.getId());
                }
            }
            
            // Generar token JWT para nuestro sistema
            String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name());
            return new AuthResponse(token, usuario);
            
        } catch (FirebaseAuthException e) {
            logger.error("Error validando token de Firebase: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid Firebase token: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("Firebase no está inicializado: {}", e.getMessage(), e);
            throw new IllegalStateException("Firebase no está configurado correctamente en el servidor. Por favor, contacte al administrador.");
        } catch (Exception e) {
            logger.error("Error inesperado en loginWithFirebase: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar la autenticación: " + e.getMessage());
        }
    }
    
    /**
     * Genera un username único basado en el email
     */
    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        
        if (baseUsername.length() < 3) {
            baseUsername = "user" + baseUsername;
        }
        
        String username = baseUsername;
        int suffix = 1;
        
        // Asegurar que el username sea único
        while (usuarioRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        
        return username;
    }
    
    /**
     * Determina el rol del usuario basado en reglas de negocio
     * - Si es el primer usuario en el sistema, es ADMIN
     * - Si el email está en la lista de admins configurados, es ADMIN
     * - Por defecto, es LECTOR
     */
    private Usuario.UserRole determineUserRole(String email) {
        // Si es el primer usuario del sistema, hacerlo ADMIN
        long totalUsuarios = usuarioRepository.count();
        if (totalUsuarios == 0) {
            logger.info("Primer usuario del sistema - asignando rol ADMIN a: {}", email);
            return Usuario.UserRole.ADMIN;
        }
        
        // Por defecto, todos los usuarios OAuth son LECTOR
        // Un ADMIN puede cambiar el rol después desde el panel de administración
        return Usuario.UserRole.LECTOR;
    }
}
