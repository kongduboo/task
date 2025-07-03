package com.rsupport.task.api.v1.notice.dto.response;

import com.rsupport.task.api.v1.notice.domain.Notice;

import java.time.Instant;

public record NoticeSummaryResponse(
		Long id,
		String title,
		boolean hasAttachment,
		Instant createdAt,
		int viewCount,
		String writer
) {

	public static NoticeSummaryResponse from(Notice notice) {
		return new NoticeSummaryResponse(
				notice.getId(),
				notice.getTitle(),
				!notice.getAttachments().isEmpty(),
				notice.getCreatedAt(),
				notice.getViewCount(),
				notice.getWriter()
		);
	}
}
