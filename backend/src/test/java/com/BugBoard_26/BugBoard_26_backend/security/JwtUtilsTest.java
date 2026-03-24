package com.BugBoard_26.BugBoard_26_backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per JwtUtils.
 *
 * Metodi testati:
 * - isTokenValid(String token, UserDetails userDetails)
 * - generateToken(UserDetails userDetails)
 *
 * Strategia: Istanziazione diretta della classe di utilità.
 * Utilizzo di ReflectionTestUtils per iniettare i valori
 * di @Value (secretKey, jwtExpiration) simulando l'environment.
 * Nessun contesto Spring o Mockito necessario.
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetails utenteBase;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();

        ReflectionTestUtils.setField(jwtUtils, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", 86400000L); // 1 giorno

        utenteBase = new User("mario.rossi", "password123", new ArrayList<>());
    }

    /**
     * EC1 – Token valido, e UserDetails corrisponde all'owner del token.
     * Il metodo isTokenValid deve restituire true.
     */
    @Test
    void isTokenValid_utenteCorretto_ritornaTrue() {
        String token = jwtUtils.generateToken(utenteBase);

        boolean isValid = jwtUtils.isTokenValid(token, utenteBase);

        assertTrue(isValid, "Il token deve risultare valido per l'utente che lo ha generato");
    }

    /**
     * EC2 – Token valido, ma UserDetails passato appartiene a un altro utente.
     * Il metodo isTokenValid deve restituire false.
     */
    @Test
    void isTokenValid_utenteDiverso_ritornaFalse() {
        String tokenMario = jwtUtils.generateToken(utenteBase);
        UserDetails utenteDiverso = new User("luigi.verdi", "password123", new ArrayList<>());

        boolean isValid = jwtUtils.isTokenValid(tokenMario, utenteDiverso);

        assertFalse(isValid, "Il token non deve risultare valido se verificato contro un utente diverso");
    }

    /**
     * EC3 – Token con data di scadenza nel passato.
     * La libreria JWT deve intercettarlo e lanciare ExpiredJwtException.
     */
    @Test
    void isTokenValid_tokenScaduto_lanciaEccezione() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", -1000L);
        String tokenScaduto = jwtUtils.generateToken(utenteBase);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtils.isTokenValid(tokenScaduto, utenteBase);
        }, "Se il token è scaduto, la validazione deve lanciare ExpiredJwtException");
    }
    
    /**
     * EC4 – Token malformato (una stringa a caso che non è un JWT).
     * Deve lanciare MalformedJwtException.
     */
    @Test
    void isTokenValid_tokenMalformato_lanciaEccezione() {
        String tokenFinto = "ciao-sono-un-hacker-e-questo-non-e-un-token";

        assertThrows(MalformedJwtException.class, () -> {
            jwtUtils.isTokenValid(tokenFinto, utenteBase);
        }, "Una stringa a caso deve lanciare MalformedJwtException");
    }

    /**
     * EC5 – Token vuoto.
     * Deve lanciare IllegalArgumentException.
     */
    @Test
    void isTokenValid_tokenVuoto_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtils.isTokenValid("", utenteBase);
        }, "Un token vuoto o nullo deve lanciare IllegalArgumentException");
    }

    /**
     * EC6 – Token con firma manomessa (creato con una chiave segreta diversa).
     * Deve lanciare SignatureException.
     */
    @Test
    void isTokenValid_firmaInvalida_lanciaEccezione() {

        String chiaveSbagliata = "ChiaveSbagliataMoltoLungaPerSimulareUnAttaccoHacker1234567890";
        String tokenManomesso = io.jsonwebtoken.Jwts.builder()
                .setSubject(utenteBase.getUsername())
                .signWith(
                        io.jsonwebtoken.security.Keys
                                .hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(chiaveSbagliata)),
                        io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        assertThrows(SignatureException.class, () -> {
            jwtUtils.isTokenValid(tokenManomesso, utenteBase);
        }, "Un token firmato con la chiave sbagliata deve essere respinto con SignatureException");
    }
}