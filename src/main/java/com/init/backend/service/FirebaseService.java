package com.init.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);
    
    @Value("${firebase.project-id:}")
    private String firebaseProjectId;
    
    @Value("${firebase.credentials:}")
    private String firebaseCredentials;
    
    @Value("${firebase.credentials.path:firebase-credentials.json}")
    private String firebaseCredentialsPath;
    
    private FirebaseAuth firebaseAuth;
    
    @PostConstruct
    public void initialize() {
        try {
            InputStream credentialsStream = null;
            
            // Prioridad 1: Usar credenciales desde string JSON en application.properties
            if (firebaseCredentials != null && !firebaseCredentials.isEmpty()) {
                logger.info("Inicializando Firebase desde credentials string en application.properties");
                credentialsStream = new ByteArrayInputStream(firebaseCredentials.getBytes());
            } 
            // Prioridad 2: Usar archivo desde classpath (resources)
            else {
                try {
                    Resource resource = new ClassPathResource(firebaseCredentialsPath);
                    if (resource.exists()) {
                        logger.info("Inicializando Firebase desde archivo: {}", firebaseCredentialsPath);
                        credentialsStream = resource.getInputStream();
                    } else {
                        logger.warn("Archivo de credenciales no encontrado en classpath: {}", firebaseCredentialsPath);
                    }
                } catch (Exception e) {
                    logger.warn("No se pudo cargar el archivo de credenciales desde classpath: {}", e.getMessage());
                }
            }
            
            // Si tenemos credenciales, inicializar Firebase
            if (credentialsStream != null) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
                
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(credentials);
                
                // Usar project-id de properties o del archivo JSON
                String projectId = firebaseProjectId != null && !firebaseProjectId.isEmpty() 
                        ? firebaseProjectId 
                        : null;
                if (projectId != null) {
                    optionsBuilder.setProjectId(projectId);
                }
                
                FirebaseOptions options = optionsBuilder.build();
                
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    logger.info("Firebase inicializado correctamente. Project ID: {}", 
                            projectId != null ? projectId : "desde credenciales");
                } else {
                    logger.info("Firebase ya estaba inicializado");
                }
                
                credentialsStream.close();
            } else {
                logger.warn("No se encontraron credenciales de Firebase. Login con OAuth no funcionará.");
                logger.warn("Configura firebase.project-id y firebase.credentials en application.properties o coloca firebase-credentials.json en src/main/resources");
            }
            
            firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth != null) {
                logger.info("Firebase initialized successfully");
            } else {
                logger.warn("Firebase Auth instance is null after initialization");
            }
        } catch (IOException e) {
            logger.error("Error initializing Firebase (IO): {}", e.getMessage(), e);
            firebaseAuth = null;
        } catch (IllegalArgumentException e) {
            logger.error("Error initializing Firebase (Illegal Argument): {}", e.getMessage(), e);
            firebaseAuth = null;
        } catch (Exception e) {
            logger.error("Unexpected error initializing Firebase: {}", e.getMessage(), e);
            firebaseAuth = null;
        }
    }
    
    /**
     * Verifica y decodifica un token de Firebase
     */
    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        if (firebaseAuth == null) {
            logger.error("Firebase Auth is not initialized. Please check your Firebase configuration in application.properties");
            throw new IllegalStateException("Firebase not initialized. Please configure Firebase credentials in application.properties (firebase.project-id and firebase.credentials)");
        }
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Verifica si Firebase está inicializado
     */
    public boolean isInitialized() {
        return firebaseAuth != null;
    }
}

