package com.BugBoard_26.BugBoard_26_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test unitari per JwtAuthenticationFilter.
 *
 * Strategia: mock di JwtUtils, UserDetailsService e FilterChain.
 * Utilizzo di MockHttpServletRequest/Response per simulare le chiamate web.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails utenteMock;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();


        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * EC1 - Nessun header Authorization.
     * Il filtro deve passare la palla al prossimo senza fare nulla (SecurityContext
     * vuoto).
     */
    @Test
    void doFilterInternal_senzaHeader_nonAutentica() throws ServletException, IOException {
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Il contesto di sicurezza deve essere vuoto");

        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * EC2 - Header presente, ma non inizia con "Bearer ".
     * Il filtro deve ignorarlo.
     */
    @Test
    void doFilterInternal_headerSbagliato_nonAutentica() throws ServletException, IOException {
        request.addHeader("Authorization", "TokenStrano 12345");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * EC3 - Token corretto e valido.
     * L'utente deve essere caricato e registrato nel SecurityContext.
     */
    @Test
    void doFilterInternal_tokenValido_autenticaUtente() throws ServletException, IOException {
        
        String fintoToken = "un-token-jwt-lunghissimo-e-valido";
        request.addHeader("Authorization", "Bearer " + fintoToken);

        
        when(jwtUtils.extractUsername(fintoToken)).thenReturn("mario.rossi");
        when(userDetailsService.loadUserByUsername("mario.rossi")).thenReturn(utenteMock);
        when(jwtUtils.isTokenValid(fintoToken, utenteMock)).thenReturn(true);


        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

       
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "L'utente deve essere autenticato");
        assertEquals("mario.rossi", SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain, times(1)).doFilter(request, response);
    }
}