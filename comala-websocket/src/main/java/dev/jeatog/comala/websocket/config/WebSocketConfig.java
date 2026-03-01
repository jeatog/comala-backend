package dev.jeatog.comala.websocket.config;

import dev.jeatog.comala.websocket.interceptores.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para los destinos manejados por el broker (suscripciones de cliente)
        registry.enableSimpleBroker("/ws");
        // Prefijo para los mensajes dirigidos a @MessageMapping en los handlers
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
