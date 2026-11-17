package org.scoutsdecanarias.ecatlim_backend.core.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.scoutsdecanarias.ecatlim_backend.core.UserDetailsServiceImpl;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** The only endpoints a session can use while the user still has to change a temporary password. */
    private static final Set<String> PASSWORD_CHANGE_PATHS = Set.of("/password/change-password", "/auth/refresh", "/auth/logout", "/user/me");

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    /** Login, refresh and second factor steps are called without a session, so a stale token must not block them. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authorizationHeader.substring(7);
        String username;

        try {
            username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (Boolean.TRUE.equals(jwtUtil.validateToken(jwt, userDetails))) {
                    if (jwtUtil.isPasswordChangeRequired(jwt) && !PASSWORD_CHANGE_PATHS.contains(request.getRequestURI())) {
                        writeError(response, HttpServletResponse.SC_FORBIDDEN, "Debes cambiar tu contraseña antes de continuar");
                        return;
                    }
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    writeErrorResponse(response, "La sesión no es válida, vuelva a iniciar sesión");
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            writeErrorResponse(response, "La sesión ha expirado, vuelva a iniciar sesión");
        } catch (JwtException e) {
            writeErrorResponse(response, "La sesión no es válida, vuelva a iniciar sesión");
        } catch (Exception e) {
            logger.error("Error during JWT authentication", e);
            writeErrorResponse(response, "La sesión no es válida, vuelva a iniciar sesión");
        }
    }

    private static void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    private static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(new EcatlimErrorResponse(message)));
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
