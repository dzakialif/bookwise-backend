package com.dzaki.bookwise.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtil {
    
    @Value("${bookwise.jwt.secret}")
    private String secret;

    @Value("${bookwise.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractName(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public String generateAccessToken(String name) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, name, accessTokenExpiration);
    }

    // Overload methods untuk generate token dengan email
    public String generateAccessTokenWithEmail(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email, accessTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    public Boolean validateToken(String token, String name){
        final String extractedName = extractName(token);
        return (extractedName.equals(name) && !isTokenExpired(token));
    }

    public Boolean isTokenValid(String token){
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
