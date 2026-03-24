package com.BugBoard_26.BugBoard_26_backend.service;

import com.BugBoard_26.BugBoard_26_backend.dto.AuthResponse;
import com.BugBoard_26.BugBoard_26_backend.dto.LoginRequest;
import com.BugBoard_26.BugBoard_26_backend.model.Role;
import com.BugBoard_26.BugBoard_26_backend.model.User;
import com.BugBoard_26.BugBoard_26_backend.repository.UserRepository;
import com.BugBoard_26.BugBoard_26_backend.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test unitari per AuthService.
 *
 * Metodo testato:
 * - login(LoginRequest request)
 *
 * Strategia: mock di UserRepository, AuthenticationManager e JwtUtils.
 * Nessun database reale viene toccato.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User utenteMock;
    private LoginRequest loginRequestMock;

    @BeforeEach
    void setUp() {
        
        utenteMock = new User();
        utenteMock.setId(1L);
        utenteMock.setName("Mario");
        utenteMock.setEmail("mario@email.com");
        utenteMock.setPassword("passwordCodificata123");
        utenteMock.setRole(Role.STANDARD); 


        loginRequestMock = new LoginRequest();
        loginRequestMock.setEmail("mario@email.com");
        loginRequestMock.setPassword("passwordInChiaro");
    }

    /**
     * EC1 – Login corretto: email e password giuste.
     * Il flusso deve completarsi e restituire un AuthResponse con il token.
     */
    @Test
    void login_credenzialiCorrette_ritornaAuthResponse() {
    
        when(authenticationManager.authenticate(any())).thenReturn(null);

        when(repository.findByEmail("mario@email.com")).thenReturn(Optional.of(utenteMock));

        
        when(jwtUtils.generateToken(any(), eq(utenteMock))).thenReturn("finto-token-jwt-123");


        AuthResponse response = authService.login(loginRequestMock);


        assertNotNull(response, "La risposta non deve essere nulla");
        assertEquals("finto-token-jwt-123", response.getToken(),
                "Il token restituito deve coincidere con quello generato");
        assertEquals(1L, response.getUserId(), "L'ID utente deve coincidere");


        verify(authenticationManager, times(1)).authenticate(any());
        verify(repository, times(1)).findByEmail("mario@email.com");
    }

    /**
     * EC2 – Login fallito: password errata.
     * L'AuthenticationManager lancia un'eccezione e il processo si interrompe
     * subito.
     */
    @Test
    void login_passwordErrata_lanciaEccezione() {

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenziali non valide"));

        
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequestMock);
        }, "Se la password è sbagliata, deve essere lanciata una BadCredentialsException");

       
        verify(repository, never()).findByEmail(anyString());
        verify(jwtUtils, never()).generateToken(any(), any());
    }
}