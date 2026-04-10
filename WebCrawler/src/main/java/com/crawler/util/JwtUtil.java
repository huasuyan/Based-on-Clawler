package com.crawler.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT令牌
 */
@Component
public class JwtUtil {

    // 密钥，建议在配置文件中设置
    @Value("${jwt.secret:default-secret-key}")
    private static String secret;

    // 令牌过期时间（毫秒），默认24小时
    @Value("${jwt.expiration:86400000}")
    private static long expiration;

    /**
     * 生成JWT令牌
     * @param subject 令牌主题（通常是用户ID）
     * @param claims 自定义声明
     * @return JWT令牌字符串
     */
    public static String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)  // 设置自定义声明
                .setSubject(subject)  // 设置主题
                .setIssuedAt(now)  // 设置签发时间
                .setExpiration(expireDate)  // 设置过期时间
                .signWith(SignatureAlgorithm.HS256, secret)  // 设置签名算法和密钥
                .compact();  // 生成令牌
    }

    /**
     * 生成JWT令牌（简化版）
     * @param subject 令牌主题（通常是用户ID）
     * @return JWT令牌字符串
     */
    public String generateToken(String subject) {
        return generateToken(subject, null);
    }

    /**
     * 解析JWT令牌
     * @param token JWT令牌字符串
     * @return 令牌中的声明
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)  // 设置密钥
                .parseClaimsJws(token)  // 解析令牌
                .getBody();  // 获取声明
    }

    /**
     * 从令牌中获取主题（用户ID）
     * @param token JWT令牌字符串
     * @return 主题（用户ID）
     */
    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从令牌中获取自定义声明
     * @param token JWT令牌字符串
     * @param key 声明键名
     * @return 声明值
     */
    public Object getClaim(String token, String key) {
        return parseToken(token).get(key);
    }

    /**
     * 验证令牌是否过期
     * @param token JWT令牌字符串
     * @return 是否过期
     */
    public boolean isExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证令牌是否有效
     * @param token JWT令牌字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
