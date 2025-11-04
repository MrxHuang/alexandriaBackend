package com.init.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseAuthRequest {
    
    @NotBlank(message = "ID Token is required")
    private String idToken;
    
    @NotBlank(message = "Provider is required")
    private String provider;
    
    private String email;
    private String name;
    private String photoUrl;
    private String role; // ADMIN o LECTOR
}

