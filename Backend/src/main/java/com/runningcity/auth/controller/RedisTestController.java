package com.runningcity.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisConnectionFactory redisConnectionFactory;

    @GetMapping("/test")
    public Map<String, Object> testRedis() {
        Map<String, Object> result = new HashMap<>();

        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();
            connection.close();

            result.put("status", "SUCCESS");
            result.put("ping", pong);

            log.info("✅ [Redis 연결 성공] PONG 응답");

        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error", e.getMessage());

            log.error("❌ [Redis 연결 실패]", e);
        }

        return result;
    }
}