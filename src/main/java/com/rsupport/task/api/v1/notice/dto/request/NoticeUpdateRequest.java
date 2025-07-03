package com.rsupport.task.api.v1.notice.dto.request;

import java.time.Instant;

public record NoticeUpdateRequest(
		String title,
		String content,
		Instant startAt,
		Instant endAt,
		String writer
) {

}
