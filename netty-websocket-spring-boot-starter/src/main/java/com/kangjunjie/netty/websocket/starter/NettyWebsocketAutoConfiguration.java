package com.kangjunjie.netty.websocket.starter;

import com.kangjunjie.netty.websocket.starter.support.WebSocketAnnotationPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author KangJunjie
 */
@Configuration
public class NettyWebsocketAutoConfiguration {

    @Bean
    public WebSocketAnnotationPostProcessor webSocketAnnotationPostProcessor() {
        return new WebSocketAnnotationPostProcessor();
    }

    @Bean
    public WebsocketProperties websocketProperties() {
        return new WebsocketProperties();
    }
}
