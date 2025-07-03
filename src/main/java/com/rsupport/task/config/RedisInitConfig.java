package com.rsupport.task.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class RedisInitConfig {

	private final RedisTemplate<String, String> redisTemplate;

	@Bean
	public ApplicationRunner clearAllRedisKeys() {
		return args -> {
			Set<String> keys = redisTemplate.keys("*");
			if (!keys.isEmpty()) {
				redisTemplate.delete(keys);
				System.out.println("> Redis 초기화 완료: " + keys.size() + "개 키 삭제됨");
			}
		};
	}
}