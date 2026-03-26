package com.BugBoard_26.BugBoard_26_backend.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService; // Il tuo UserDetailsServiceImpl

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabilita CSRF (non serve con JWT)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Abilita CORS per Angular
                .authorizeHttpRequests(auth -> auth
                        // 1. Le porte APERTE a tutti
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // 2. Le porte solo per ADMIN (Gestione Utenti)
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // 3. Tutto il resto richiede autenticazione
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Non usare sessioni server, usa solo
                                                                                // JWT
                )
                .authenticationProvider(authenticationProvider()) // Dice come controllare la password
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Aggiunge il tuo filtro

        return http.build();
    }

    // --- CONFIGURAZIONE CORS (Collego Angular con Spring) ---
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost", "https://bug-board-26.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // --- BEAN PER LA GESTIONE PASSWORD E LOGIN ---

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // 1. Crea il provider con UserDetailsService (ora richiesto nel costruttore)
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // 3. Imposta l'algoritmo di crittografia (BCrypt)
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Cifra le password in modo sicuro
    }
}