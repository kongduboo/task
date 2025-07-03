package com.rsupport.task.api.v1.notice.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record NoticeCreateRequest(
		@NotBlank(message = "제목은 필수입니다.")
		String title,
		@NotBlank(message = "내용은 필수입니다.")
		String content,
		@NotNull(message = "시작일은 필수입니다.")
		Instant startAt,
		@NotNull(message = "종료일은 필수입니다.")
		Instant endAt,
		@NotBlank(message = "작성자는 필수입니다.")
		String writer
) {

	@AssertTrue(message = "시작일은 종료일보다 이전이어야 합니다.")
	public boolean isValidPeriod() {
		return startAt == null || endAt == null || !startAt.isAfter(endAt);
	}
}
