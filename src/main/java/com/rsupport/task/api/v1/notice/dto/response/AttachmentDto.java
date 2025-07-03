package com.rsupport.task.api.v1.notice.dto.response;

import com.rsupport.task.api.v1.notice.domain.NoticeAttachment;

public record AttachmentDto(
		Long id,
		String fileName,
		String url
) {

	public static AttachmentDto from(NoticeAttachment attachment) {
		return new AttachmentDto(
				attachment.getId(),
				attachment.getFileName(),
				attachment.getFilePath()
		);
	}
}
