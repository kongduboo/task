package com.rsupport.task.api.v1.notice.dto.request;

import java.time.Instant;

public record NoticeSearchCondition(
		String keyword,
		Boolean titleOnly,
		Instant startDate,
		Instant endDate
) {

}
