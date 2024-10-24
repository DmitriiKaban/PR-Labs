package com.app.lab2.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Component
@EnableWebSocket
public class CommandWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(commandWebSocketHandler(), "/commands").setAllowedOrigins("*");
    }

    @Bean
    public CommandWebSocketHandler commandWebSocketHandler() {
        return new CommandWebSocketHandler();
    }
}

