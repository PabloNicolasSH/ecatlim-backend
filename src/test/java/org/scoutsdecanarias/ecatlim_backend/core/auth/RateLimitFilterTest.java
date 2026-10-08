package org.scoutsdecanarias.ecatlim_backend.core.auth;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private final AtomicLong nanos = new AtomicLong(0);
    private final Ticker fakeTicker = nanos::get;

    private MockHttpServletResponse call(RateLimitFilter filter, MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private static MockHttpServletRequest login(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setRemoteAddr(ip);
        return request;
    }

    private RateLimitFilter loginFilter(int max) {
        return new RateLimitFilter(List.of(new RateLimitFilter.Rule("login-ip", "POST", "/auth/login", max,
                Duration.ofMinutes(1), r -> RateLimitFilter.clientIp(r, false))), false, fakeTicker);
    }

    @Test
    void allowsUpToTheLimitThenAnswers429WithRetryAfterAndMessage() throws Exception {
        RateLimitFilter filter = loginFilter(3);

        for (int i = 0; i < 3; i++) {
            assertThat(call(filter, login("1.1.1.1")).getStatus()).isEqualTo(200);
        }
        MockHttpServletResponse blocked = call(filter, login("1.1.1.1"));

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
        assertThat(blocked.getContentAsString()).contains("ecatlimMessage").contains("demasiados intentos");
    }

    @Test
    void countsEachClientSeparately() throws Exception {
        RateLimitFilter filter = loginFilter(1);

        assertThat(call(filter, login("1.1.1.1")).getStatus()).isEqualTo(200);
        assertThat(call(filter, login("1.1.1.1")).getStatus()).isEqualTo(429);
        assertThat(call(filter, login("2.2.2.2")).getStatus()).isEqualTo(200);
    }

    @Test
    void windowResetsAfterItExpires() throws Exception {
        RateLimitFilter filter = loginFilter(1);

        assertThat(call(filter, login("1.1.1.1")).getStatus()).isEqualTo(200);
        assertThat(call(filter, login("1.1.1.1")).getStatus()).isEqualTo(429);

        nanos.addAndGet(Duration.ofSeconds(61).toNanos());

        assertThat(call(filter, login("1.1.1.1")).getStatus()).isEqualTo(200);
    }

    @Test
    void ignoresOtherEndpointsAndMethods() throws Exception {
        RateLimitFilter filter = loginFilter(1);
        call(filter, login("1.1.1.1"));

        MockHttpServletRequest get = new MockHttpServletRequest("GET", "/auth/login");
        get.setRemoteAddr("1.1.1.1");
        MockHttpServletRequest other = new MockHttpServletRequest("POST", "/events/add");
        other.setRemoteAddr("1.1.1.1");

        assertThat(call(filter, get).getStatus()).isEqualTo(200);
        assertThat(call(filter, other).getStatus()).isEqualTo(200);
    }

    @Test
    void forgotPasswordIsLimitedPerEmailEvenFromDifferentIps() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(RateLimitFilter.defaultRules(false), false, fakeTicker);

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/password/forgot");
            request.setRemoteAddr("10.0.0." + i);
            request.setParameter("email", "Victim@Example.com");
            assertThat(call(filter, request).getStatus()).isEqualTo(200);
        }
        MockHttpServletRequest fourth = new MockHttpServletRequest("GET", "/password/forgot");
        fourth.setRemoteAddr("10.0.0.99");
        fourth.setParameter("email", "victim@example.com");

        assertThat(call(filter, fourth).getStatus()).isEqualTo(429);
    }

    @Test
    void clientIpUsesLastForwardedForHopOnlyWhenTrusted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("169.254.0.1");
        request.addHeader("X-Forwarded-For", "6.6.6.6, 203.0.113.7:51234");

        assertThat(RateLimitFilter.clientIp(request, true)).isEqualTo("203.0.113.7");
        assertThat(RateLimitFilter.clientIp(request, false)).isEqualTo("169.254.0.1");
    }

    @Test
    void clientIpKeepsIpv6Addresses() {
        MockHttpServletRequest plain = new MockHttpServletRequest();
        plain.addHeader("X-Forwarded-For", "2001:db8::1");
        MockHttpServletRequest withPort = new MockHttpServletRequest();
        withPort.addHeader("X-Forwarded-For", "[2001:db8::1]:443");

        assertThat(RateLimitFilter.clientIp(plain, true)).isEqualTo("2001:db8::1");
        assertThat(RateLimitFilter.clientIp(withPort, true)).isEqualTo("2001:db8::1");
    }
}
