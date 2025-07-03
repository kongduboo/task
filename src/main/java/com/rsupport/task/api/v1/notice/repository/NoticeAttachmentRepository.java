package com.rsupport.task.api.v1.notice.repository;

import com.rsupport.task.api.v1.notice.domain.NoticeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachment, Long> {

	void deleteByNoticeId(Long noticeId);
}
