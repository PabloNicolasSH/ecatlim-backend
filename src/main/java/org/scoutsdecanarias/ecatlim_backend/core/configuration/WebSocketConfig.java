package org.scoutsdecanarias.ecatlim_backend.configuration;

import org.scoutsdecanarias.ecatlim_backend.core.auth.JwtHandshakeInterceptor;
import org.scoutsdecanarias.ecatlim_backend.core.auth.StompAuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final StompAuthorizationInterceptor stompAuthorizationInterceptor;

    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor, StompAuthorizationInterceptor stompAuthorizationInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.stompAuthorizationInterceptor = stompAuthorizationInterceptor;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthorizationInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        String username = (String) attributes.get("username");
                        return username == null ? null : () -> username;
                    }
                })
                .setAllowedOriginPatterns("*");
    }
}
