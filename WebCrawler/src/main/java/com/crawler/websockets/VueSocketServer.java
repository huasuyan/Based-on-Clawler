package com.crawler.websockets;


import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/vue")
public class VueSocketServer {

    // 在线前端
    public static final Map<String, Session> VUE_SESSIONS = new ConcurrentHashMap<>();

    // 绑定：前端ID -> PythonID
    public static final Map<String, String> VUE_TO_PYTHON = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        VUE_SESSIONS.put(session.getId(), session);
        log.info("【前端上线】id={}，在线数={}", session.getId(), VUE_SESSIONS.size());
    }

    // 前端发消息 → 转发给 Python
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("【前端消息】{}", message);

        // 获取绑定的 Python
        String pythonId = VUE_TO_PYTHON.get(session.getId());
        if (pythonId == null) {
            sendMessage(session, "未绑定Python客户端");
            return;
        }

        // 转发给 Python
        PythonSocketServer.sendToPython(pythonId, message);
    }

    @OnClose
    public void onClose(Session session) {
        VUE_SESSIONS.remove(session.getId());
        VUE_TO_PYTHON.remove(session.getId());
        log.info("【前端下线】id={}", session.getId());
    }

    // 发送消息给前端
    public static void sendToVue(String vueId, String message) {
        try {
            Session session = VUE_SESSIONS.get(vueId);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
        }
    }
}