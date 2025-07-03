package com.rsupport.task.api.v1.notice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisViewCountService {

	private static final String PREFIX = "notice:viewcount:";
	private final RedisTemplate<String, String> redisTemplate;

	public void increaseViewCount(Long noticeId) {
		redisTemplate.opsForValue().increment(PREFIX + noticeId);
	}

	public int getViewCount(Long noticeId) {
		String value = redisTemplate.opsForValue().get(PREFIX + noticeId);
		return value != null ? Integer.parseInt(value) : 0;
	}

	public Set<String> getAllKeys() {
		return redisTemplate.keys(PREFIX + "*");
	}

	public int getAndDelete(String key) {
		String value = redisTemplate.opsForValue().get(key);
		redisTemplate.delete(key);
		return value != null ? Integer.parseInt(value) : 0;
	}

	public void deleteViewCount(Long noticeId) {
		redisTemplate.delete(PREFIX + noticeId);
	}

	public Long extractIdFromKey(String key) {
		return Long.parseLong(key.replace(PREFIX, ""));
	}
}