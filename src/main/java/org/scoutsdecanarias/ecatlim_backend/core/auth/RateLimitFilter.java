package org.scoutsdecanarias.ecatlim_backend.core.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public record Rule(String name, String method, String path, int maxRequests, Duration window,
                       Function<HttpServletRequest, String> keyExtractor) {}

    private record Window(long startNanos, AtomicInteger count) {}

    private record LimitedRule(Rule rule, Cache<String, Window> windows) {}

    private final List<LimitedRule> rules;
    private final boolean trustForwardedFor;
    private final Ticker ticker;

    public RateLimitFilter(List<Rule> rules, boolean trustForwardedFor) {
        this(rules, trustForwardedFor, Ticker.systemTicker());
    }

    RateLimitFilter(List<Rule> rules, boolean trustForwardedFor, Ticker ticker) {
        this.trustForwardedFor = trustForwardedFor;
        this.ticker = ticker;
        this.rules = rules.stream()
                .map(rule -> new LimitedRule(rule, Caffeine.newBuilder()
                        .expireAfterWrite(rule.window())
                        .maximumSize(100_000)
                        .ticker(ticker)
                        .<String, Window>build()))
                .toList();
    }

    public static List<Rule> defaultRules(boolean trustForwardedFor) {
        Function<HttpServletRequest, String> byIp = request -> clientIp(request, trustForwardedFor);
        Function<HttpServletRequest, String> byEmail = request -> {
            String email = request.getParameter("email");
            return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        };
        return List.of(
                new Rule("login-ip", "POST", "/auth/login", 10, Duration.ofMinutes(1), byIp),
                new Rule("forgot-ip", "GET", "/password/forgot", 5, Duration.ofMinutes(15), byIp),
                new Rule("forgot-email", "GET", "/password/forgot", 3, Duration.ofHours(1), byEmail),
                new Rule("reset-ip", "POST", "/password/reset", 10, Duration.ofMinutes(15), byIp),
                new Rule("pending-user-ip", "POST", "/pending-user/request", 5, Duration.ofHours(1), byIp),
                new Rule("scout-groups-ip", "GET", "/scout-group/all", 60, Duration.ofMinutes(1), byIp)
        );
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        for (LimitedRule limited : rules) {
            Rule rule = limited.rule();
            if (!rule.method().equalsIgnoreCase(request.getMethod()) || !rule.path().equals(path)) {
                continue;
            }
            String key = rule.keyExtractor().apply(request);
            if (key == null || key.isBlank()) {
                continue;
            }

            long now = ticker.read();
            Window window = limited.windows().get(key, k -> new Window(now, new AtomicInteger()));
            if (window.count().incrementAndGet() > rule.maxRequests()) {
                long elapsedSeconds = Duration.ofNanos(now - window.startNanos()).toSeconds();
                long retryAfterSeconds = Math.max(1, rule.window().toSeconds() - elapsedSeconds);
                log.warn("METHOD doFilterInternal() - Rate limit '{}' exceeded on {} {}", rule.name(), request.getMethod(), path);
                writeTooManyRequests(response, retryAfterSeconds);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        long minutes = Math.max(1, (retryAfterSeconds + 59) / 60);
        String message = "Has realizado demasiados intentos. Vuelve a intentarlo en %d %s"
                .formatted(minutes, minutes == 1 ? "minuto" : "minutos");

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(new EcatlimErrorResponse(message)));
    }

    static String clientIp(HttpServletRequest request, boolean trustForwardedFor) {
        if (trustForwardedFor) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String[] hops = forwardedFor.split(",");
                return stripPort(hops[hops.length - 1].trim());
            }
        }
        return request.getRemoteAddr();
    }

    private static String stripPort(String address) {
        if (address.startsWith("[")) {
            int end = address.indexOf(']');
            return end > 0 ? address.substring(1, end) : address;
        }
        int colon = address.indexOf(':');
        boolean ipv4WithPort = colon > 0 && colon == address.lastIndexOf(':');
        return ipv4WithPort ? address.substring(0, colon) : address;
    }
}
