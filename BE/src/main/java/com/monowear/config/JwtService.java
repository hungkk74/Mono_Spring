package com.monowear.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Component
@Slf4j
public class JwtService {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration-seconds}")
    private long expirationSeconds;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    void init() {
        try {
            this.privateKey = loadPrivateKey(privateKeyResource);
            this.publicKey = loadPublicKey(publicKeyResource);
            log.info("JWT RSA keys loaded successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT RSA keys", e);
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    // Tạo access token
    public String generateToken(Long userId, String email, String role, String fullName) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("upn", email)
                .claim("groups", Set.of(role))
                .claim("full_name", fullName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(privateKey)
                .compact();
    }

    // Tạo reset token
    public String generateResetToken(Long userId, String email, long lifespanSeconds) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .claim("purpose", "password_reset")
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + lifespanSeconds * 1000))
                .signWith(privateKey)
                .compact();
    }

    // Parse và validate token
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }


    @SuppressWarnings("unchecked")
    public Set<String> getRoles(Claims claims) {
        Object groups = claims.get("groups");
        if (groups instanceof java.util.Collection<?> col) {
            return new java.util.HashSet<>((java.util.Collection<String>) col);
        }
        return Set.of();
    }



    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "")
                 .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                 .replace("-----END RSA PRIVATE KEY-----", "")
                 .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                 .replace("-----END PUBLIC KEY-----", "")
                 .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
