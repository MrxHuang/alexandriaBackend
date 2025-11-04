package com.init.backend.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DatabaseConnectionVerifier {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionVerifier.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private Environment environment;
    
    @PostConstruct
    public void verifyDatabaseConnection() {
        logger.info("=========================================");
        logger.info("VERIFICANDO CONEXIÓN A BASE DE DATOS");
        logger.info("=========================================");
        
        // Mostrar configuración de la base de datos
        String dbUrl = environment.getProperty("spring.datasource.url");
        String dbUsername = environment.getProperty("spring.datasource.username");
        String dbDriver = environment.getProperty("spring.datasource.driver-class-name");
        
        logger.info("URL de conexión: {}", dbUrl);
        logger.info("Usuario: {}", dbUsername);
        logger.info("Driver: {}", dbDriver);
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                logger.info("✓ CONEXIÓN ESTABLECIDA EXITOSAMENTE");
                
                DatabaseMetaData metaData = connection.getMetaData();
                logger.info("Base de datos: {}", metaData.getDatabaseProductName());
                logger.info("Versión: {}", metaData.getDatabaseProductVersion());
                logger.info("URL de conexión real: {}", metaData.getURL());
                logger.info("Usuario conectado: {}", metaData.getUserName());
                
                // Verificar si la base de datos existe
                String catalog = connection.getCatalog();
                logger.info("Catálogo actual: {}", catalog);
                
                // Listar tablas existentes
                logger.info("--- TABLAS EN LA BASE DE DATOS ---");
                try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                    int tableCount = 0;
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        logger.info("  - Tabla: {}", tableName);
                        tableCount++;
                    }
                    if (tableCount == 0) {
                        logger.warn("⚠ NO SE ENCONTRARON TABLAS EN LA BASE DE DATOS");
                    } else {
                        logger.info("Total de tablas encontradas: {}", tableCount);
                    }
                }
                
                // Verificar si la conexión es válida
                boolean isValid = connection.isValid(5);
                logger.info("Conexión válida: {}", isValid);
                
            } else {
                logger.error("✗ ERROR: La conexión es nula o está cerrada");
            }
            
        } catch (SQLException e) {
            logger.error("✗ ERROR AL CONECTAR A LA BASE DE DATOS", e);
            logger.error("Mensaje de error: {}", e.getMessage());
            logger.error("Código de error SQL: {}", e.getSQLState());
            logger.error("Código de error del proveedor: {}", e.getErrorCode());
            if (e.getCause() != null) {
                logger.error("Causa: {}", e.getCause().getMessage());
            }
        } catch (Exception e) {
            logger.error("✗ ERROR INESPERADO AL VERIFICAR LA CONEXIÓN", e);
        }
        
        logger.info("=========================================");
    }
}

