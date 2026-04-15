package com.crawler.config;

import com.crawler.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crawler.entity.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private JwtUtil jwtUtil;

    @Value("${jwt.token-expiration}")
    private Integer token_expiration;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 1.从请求头获取 token
        String token = request.getHeader("token");
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7);
        }

        //2.token 为空
        if (token == null || token.isEmpty()) {
            writeError(response, "未登录，请先登录");
            return false;
        }

        // 3. 验证 token 格式与签名，并检查是否过期
        try {
            jwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            writeError(response, "登录已过期，请重新登录");
            redisTemplate.delete(token);
            return false;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            writeError(response, "无效的token");
            return false;
        }

        // 4. 检查 Redis 中 token 是否仍有效（用于服务端主动踢下线）
        Object userObj = redisTemplate.opsForValue().get(token);
        if (userObj == null) {
            writeError(response, "登录已过期，请重新登录");
            return false;
        }
        // 5. 将用户信息存入 request，方便后续 Controller 使用
        request.setAttribute("currentUser", userObj);
        return true;
    }

    private void writeError(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401); //401,表示token错误
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(msg)));
    }
}