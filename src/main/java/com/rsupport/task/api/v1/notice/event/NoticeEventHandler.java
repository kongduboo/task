package com.rsupport.task.api.v1.notice.event;

import com.rsupport.task.api.v1.notice.service.RedisViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NoticeEventHandler {

	private final RedisViewCountService redisViewCountService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDeleteEvent(NoticeDeletedEvent event) {
		redisViewCountService.deleteViewCount(event.noticeId());
	}
}
