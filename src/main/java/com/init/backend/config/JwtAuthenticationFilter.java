package com.init.backend.config;

import com.init.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        logger.debug("Procesando petición: {} {}", request.getMethod(), requestPath);
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Usuario extraído del token: {}", username);
            } catch (Exception e) {
                logger.error("Error extracting username from token: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("No se encontró header Authorization o no tiene formato Bearer para la petición: {}", requestPath);
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean isValid = jwtUtil.validateToken(jwt, username);
            logger.debug("Token válido para usuario {}: {}", username, isValid);
            
            if (isValid) {
                String role = jwtUtil.extractRole(jwt);
                logger.debug("Rol extraído del token: {}", role);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(authority));
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("Autenticación establecida para usuario: {} con rol: {}", username, role);
            } else {
                logger.warn("Token inválido para usuario: {}", username);
            }
        } else if (username == null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.warn("No se pudo autenticar la petición a: {} - Usuario: {}, Authentication: {}", 
                    requestPath, username, SecurityContextHolder.getContext().getAuthentication());
        }
        
        filterChain.doFilter(request, response);
    }
}
