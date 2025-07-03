package com.rsupport.task.api.v1.notice.dto.response;

import com.rsupport.task.api.v1.notice.domain.Notice;

import java.time.Instant;
import java.util.List;

public record NoticeDetailResponse(
		Long id,
		String title,
		String content,
		Instant createdAt,
		int viewCount,
		String writer,
		List<AttachmentDto> attachments
) {

	public static NoticeDetailResponse from(Notice notice, int totalViewCount) {
		return new NoticeDetailResponse(
				notice.getId(),
				notice.getTitle(),
				notice.getContent(),
				notice.getCreatedAt(),
//				notice.getViewCount(),
				totalViewCount,
				notice.getWriter(),
				notice.getAttachments().stream().map(AttachmentDto::from).toList()
		);
	}
}
