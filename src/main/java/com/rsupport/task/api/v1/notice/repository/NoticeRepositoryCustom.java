package com.rsupport.task.api.v1.notice.repository;

import com.rsupport.task.api.v1.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface NoticeRepositoryCustom {
	Page<Notice> search(String keyword, boolean titleOnly, Instant startDate, Instant endDate, Pageable pageable);
}
