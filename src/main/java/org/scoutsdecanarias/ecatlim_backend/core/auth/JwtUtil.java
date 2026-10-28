package org.scoutsdecanarias.ecatlim_backend.core.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Set;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private static final String PASSWORD_VERSION_CLAIM = "pv";

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private static final Set<String> COMPROMISED_SECRET_HASHES = Set.of(
            "1b2946743e609424267401706cecb4df33e94a399c5a62aed2ad0a6a28451dc1"
    );

    @PostConstruct
    void validateSecret() throws NoSuchAlgorithmException {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) must be a Base64 encoded value", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) must be at least 256 bits (32 bytes) long");
        }
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        if (COMPROMISED_SECRET_HASHES.contains(HexFormat.of().formatHex(hash))) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) is a compromised value published in the repository. Generate a new one");
        }
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim(PASSWORD_VERSION_CLAIM, passwordVersion(userDetails))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && userDetails.isEnabled()
                && passwordVersion(userDetails).equals(extractClaim(token, claims -> claims.get(PASSWORD_VERSION_CLAIM, String.class)))
                && !isTokenExpired(token);
    }

    /** Changes with the password, so tokens issued before a password change stop working. */
    private static String passwordVersion(UserDetails userDetails) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(userDetails.getPassword().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
