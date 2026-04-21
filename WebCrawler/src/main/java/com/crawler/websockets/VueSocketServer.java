package com.crawler.websockets;


import com.crawler.config.WebSocketConfig;
import com.crawler.entity.User;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/vue",configurator = WebSocketConfig.TokenHandshake.class)
public class VueSocketServer {

    // 在线前端
    public static final Map<String, Session> VUE_SESSIONS = new ConcurrentHashMap<>();

    // 绑定：前端ID -> PythonID
    public static final Map<String, String> VUE_TO_PYTHON = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        try {
            String userId = session.getUserProperties().get("userId").toString();

            if (Objects.equals(userId, "null") || userId.isEmpty()) {
                log.error("无userId，关闭连接");
                session.close();
                return;
            }

            // 存入用户
            VUE_SESSIONS.put(userId, session);
            log.info("【前端上线】userId={}", userId);

        } catch (Exception e) {
            log.error("OnOpen异常", e);
            try { session.close(); } catch (Exception ignored) {}
        }
    }


    // 异常处理
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("【WebSocket异常】已捕获", throwable);
    }

    @OnClose
    public void onClose(Session session) {
        VUE_SESSIONS.remove(session.getId());
        VUE_TO_PYTHON.remove(session.getId());
        log.info("【前端下线】id={}", session.getId());
    }

    // 发送消息给前端
    public static void sendToVue(String userId, String message) {
        try {
            Session session = VUE_SESSIONS.get(userId);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}