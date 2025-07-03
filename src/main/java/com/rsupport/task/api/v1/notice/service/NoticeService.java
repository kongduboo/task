package com.rsupport.task.api.v1.notice.service;

import com.rsupport.task.api.v1.notice.domain.Notice;
import com.rsupport.task.api.v1.notice.domain.NoticeAttachment;
import com.rsupport.task.api.v1.notice.dto.request.NoticeCreateRequest;
import com.rsupport.task.api.v1.notice.dto.request.NoticeSearchCondition;
import com.rsupport.task.api.v1.notice.dto.request.NoticeUpdateRequest;
import com.rsupport.task.api.v1.notice.dto.response.NoticeDetailResponse;
import com.rsupport.task.api.v1.notice.dto.response.NoticeSummaryResponse;
import com.rsupport.task.api.v1.notice.event.NoticeDeletedEvent;
import com.rsupport.task.api.v1.notice.repository.NoticeRepository;
import com.rsupport.task.common.exception.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

	private final ApplicationEventPublisher eventPublisher;

	private final NoticeAttachmentService attachmentService;
	private final NoticeRepository noticeRepository;
	private final RedisViewCountService redisViewCountService;

	@Transactional
	public Long registerNotice(NoticeCreateRequest request, List<MultipartFile> attachments) {
		Notice notice = Notice.builder()
				.title(request.title())
				.content(request.content())
				.startAt(request.startAt())
				.endAt(request.endAt())
				.writer(request.writer())
				.build();

		if (attachments != null && !attachments.isEmpty()) {
			List<NoticeAttachment> savedFiles = attachmentService.storeFiles(attachments);
			savedFiles.forEach(notice::addAttachment);
		}

		return noticeRepository.save(notice).getId();
	}

	@Transactional(readOnly = true)
	public Page<NoticeSummaryResponse> getNoticeList(
			NoticeSearchCondition condition, Pageable pageable) {
		String keyword = condition.keyword();
		boolean titleOnly = Boolean.TRUE.equals(condition.titleOnly());
		Instant startDate = condition.startDate();
		Instant endDate = condition.endDate();

		return noticeRepository.search(keyword, titleOnly, startDate, endDate, pageable)
				.map(NoticeSummaryResponse::from);
	}

	@Transactional(readOnly = true)
	public NoticeDetailResponse getNoticeDetail(Long id) {
		Notice notice = noticeRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("공지사항이 존재하지 않습니다."));
		redisViewCountService.increaseViewCount(id);

		int redisCount = redisViewCountService.getViewCount(id);
		int totalViewCount = notice.getViewCount() + redisCount;

		return NoticeDetailResponse.from(notice, totalViewCount);
	}

	@Transactional
	public void applyViewCountFromRedis(Long noticeId, int delta) {
		Notice notice = noticeRepository.findById(noticeId)
				.orElseThrow(() -> new EntityNotFoundException("공지사항이 존재하지 않습니다."));
		notice.increaseViewCount(delta);
		// dirty checking으로 save 생략 가능
	}

	@Transactional
	public void updateNotice(Long id, NoticeUpdateRequest request,
			List<MultipartFile> attachments) {
		Notice notice = noticeRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("공지사항을 찾을 수 없습니다."));

		notice.update(request.title(), request.content(), request.startAt(), request.endAt(),
				request.writer());

		notice.clearAttachments(); // 기존 첨부파일 제거
		if (attachments != null && !attachments.isEmpty()) {
			List<NoticeAttachment> newAttachments = attachmentService.storeFiles(attachments);
			newAttachments.forEach(notice::addAttachment); // 양방향 연관관계 설정
		}
	}

	@Transactional
	public void deleteNotice(Long id) {
		Notice notice = noticeRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("공지사항을 찾을 수 없습니다."));

		// 연관된 첨부파일 삭제는 CascadeType.ALL + orphanRemoval=true로 엔티티에서 자동 처리됨
		noticeRepository.delete(notice);

		eventPublisher.publishEvent(new NoticeDeletedEvent(id));
	}

	@EventListener
	public void afterTransactionCommit(NoticeDeletedEvent e) {
		redisViewCountService.deleteViewCount(e.noticeId());
	}
}
