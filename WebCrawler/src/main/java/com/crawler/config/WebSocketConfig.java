package com.crawler.config;

import com.crawler.entity.User;
import jakarta.annotation.Resource;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.List;

@Configuration
public class WebSocketConfig {

    /**
     * 自动注册 @ServerEndpoint 注解的WebSocket端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {

        return new ServerEndpointExporter();
    }

    // 从请求头获取 userId
    public static class TokenHandshake extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            // 从 Sec-WebSocket-Protocol 取 userId
            List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");
            if (protocols != null && !protocols.isEmpty()) {
                String userId = protocols.get(0);
                response.getHeaders().put("Sec-WebSocket-Protocol", protocols);
                sec.getUserProperties().put("userId", userId);
            }


        }
    }
}