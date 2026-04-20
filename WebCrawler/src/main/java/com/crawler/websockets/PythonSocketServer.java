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
@ServerEndpoint("/ws/python")
public class PythonSocketServer {

    // 在线Python
    public static final Map<String, Session> PYTHON_SESSIONS = new ConcurrentHashMap<>();

    // 绑定：PythonID -> 前端ID
    public static final Map<String, String> PYTHON_TO_VUE = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        PYTHON_SESSIONS.put(session.getId(), session);
        log.info("【Python上线】id={}，在线数={}", session.getId(), PYTHON_SESSIONS.size());

        // 自动绑定：第一个Python绑定所有前端
        // （正式环境可以用 userId 绑定）
        for (String vueId : VueSocketServer.VUE_SESSIONS.keySet()) {
            PYTHON_TO_VUE.put(session.getId(), vueId);
            VueSocketServer.VUE_TO_PYTHON.put(vueId, session.getId());
            break;
        }
    }

    // Python 回消息 → 转发给前端
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("【Python消息】{}", message);

        // 获取绑定的前端
        String frontId = PYTHON_TO_VUE.get(session.getId());
        if (frontId == null) return;

        // 转发给前端
        VueSocketServer.sendToVue(frontId, message);
    }

    @OnClose
    public void onClose(Session session) {
        PYTHON_SESSIONS.remove(session.getId());
        PYTHON_TO_VUE.remove(session.getId());
        log.info("【Python下线】id={}", session.getId());
    }

    // 发送消息给Python
    public static void sendToPython(String pythonId, String message) {
        try {
            Session session = PYTHON_SESSIONS.get(pythonId);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}