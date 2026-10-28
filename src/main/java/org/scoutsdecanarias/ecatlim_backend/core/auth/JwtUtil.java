package org.scoutsdecanarias.ecatlim_backend.core.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
    private static final String PASSWORD_CHANGE_CLAIM = "pwdChange";
    private static final String TYPE_CLAIM = "typ";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_CHALLENGE = "challenge";
    private static final long CHALLENGE_EXPIRATION_MS = 5 * 60 * 1000L;

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
        return generateToken(userDetails, false);
    }

    /** A token with passwordChangeRequired only opens the endpoints needed to change the password. */
    public String generateToken(UserDetails userDetails, boolean passwordChangeRequired) {
        var builder = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim(TYPE_CLAIM, TYPE_ACCESS)
                .claim(PASSWORD_VERSION_CLAIM, passwordVersion(userDetails))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs));
        if (passwordChangeRequired) {
            builder.claim(PASSWORD_CHANGE_CLAIM, true);
        }
        return builder.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    /** Short-lived proof that the password was right, which only serves to finish the second factor step. */
    public String generateChallengeToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim(TYPE_CLAIM, TYPE_CHALLENGE)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + CHALLENGE_EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** The username a valid challenge token was issued to; fails for expired, forged or access tokens. */
    public String extractChallengeUsername(String token) {
        if (!TYPE_CHALLENGE.equals(extractClaim(token, claims -> claims.get(TYPE_CLAIM, String.class)))) {
            throw new JwtException("Not a challenge token");
        }
        return extractUsername(token);
    }

    public boolean isPasswordChangeRequired(String token) {
        return Boolean.TRUE.equals(extractClaim(token, claims -> claims.get(PASSWORD_CHANGE_CLAIM, Boolean.class)));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && TYPE_ACCESS.equals(extractClaim(token, claims -> claims.get(TYPE_CLAIM, String.class)))
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
