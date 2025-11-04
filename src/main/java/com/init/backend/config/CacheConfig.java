package com.init.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Configuración del CacheManager con Caffeine
     * Define diferentes caches para diferentes tipos de datos
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "autores",           // Cache para autores
            "autoresList",       // Cache para lista completa de autores
            "libros",            // Cache para libros individuales
            "librosByAutor",     // Cache para libros por autor
            "usuarios",          // Cache para usuarios
            "prestamos"          // Cache para préstamos individuales
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }
    
    /**
     * Configuración de Caffeine Cache
     * - Tamaño máximo: 500 entradas
     * - Tiempo de expiración: 30 minutos después del último acceso
     * - Tiempo de expiración después de escritura: 1 hora
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(500)                           // Máximo 500 entradas por cache
                .expireAfterAccess(30, TimeUnit.MINUTES)   // Expira después de 30 min sin acceso
                .expireAfterWrite(1, TimeUnit.HOURS)        // Expira después de 1 hora de escritura
                .recordStats();                             // Habilita estadísticas para monitoreo
    }
}

