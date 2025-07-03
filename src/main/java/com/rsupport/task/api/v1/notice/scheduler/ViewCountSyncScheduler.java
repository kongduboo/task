package com.rsupport.task.api.v1.notice.scheduler;

import com.rsupport.task.api.v1.notice.service.NoticeService;
import com.rsupport.task.api.v1.notice.service.RedisViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

	private final RedisViewCountService redisService;
	private final NoticeService noticeService;

	@Scheduled(fixedDelay = 60000) // 1분 간격
	public void syncRedisToDb() {
		var keys = redisService.getAllKeys();
		if (keys == null || keys.isEmpty()) {
			return;
		}

		for (String key : keys) {
			Long noticeId = redisService.extractIdFromKey(key);
			int count = redisService.getAndDelete(key);
			if (count > 0) {
				log.info("반영: noticeId={}, 증가수={}", noticeId, count);
				noticeService.applyViewCountFromRedis(noticeId, count);
			}
		}
	}
}