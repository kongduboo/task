package com.rsupport.task.api.v1.notice.service;

import com.rsupport.task.api.v1.notice.domain.Notice;
import com.rsupport.task.api.v1.notice.domain.NoticeAttachment;
import com.rsupport.task.api.v1.notice.dto.request.NoticeSearchCondition;
import com.rsupport.task.api.v1.notice.dto.response.NoticeDetailResponse;
import com.rsupport.task.api.v1.notice.dto.response.NoticeSummaryResponse;
import com.rsupport.task.api.v1.notice.repository.NoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("공지사항 조회 서비스 테스트")
@SpringBootTest
@Transactional
class NoticeServiceTest {

	@Autowired
	NoticeService noticeService;

	@Autowired
	NoticeRepository noticeRepository;

	@BeforeEach
	void setUp() {
		// 공지사항 3개 등록
		Notice n1 = createNotice("제목1", "내용1", Instant.parse("2025-06-25T00:00:00Z"));
		Notice n2 = createNotice("업데이트 공지", "긴급 내용", Instant.parse("2025-06-28T00:00:00Z"));
		Notice n3 = createNotice("이벤트", "이벤트 내용", Instant.parse("2025-06-30T00:00:00Z"));

		NoticeAttachment attachment = NoticeAttachment.builder()
				.fileName("file1.pdf")
				.filePath("/upload/file1.pdf")
				.build();

		n2.addAttachment(attachment);

		noticeRepository.saveAll(List.of(n1, n2, n3));
	}

	private Notice createNotice(String title, String content, Instant createdAt) {
		return Notice.builder()
				.title(title)
				.content(content)
				.writer("관리자")
				.startAt(createdAt)
				.endAt(createdAt.plus(3, ChronoUnit.DAYS))
				.build();
	}

	@Test
	@DisplayName("공지사항 목록 조회 성공")
	void list_success() {
		NoticeSearchCondition condition = new NoticeSearchCondition(
				null,   // keyword
				false,  // titleOnly
				null,   // startDate
				null    // endDate
		);

		Page<NoticeSummaryResponse> result = noticeService.getNoticeList(condition, PageRequest.of(0, 10));
		assertThat(result.getTotalElements()).isEqualTo(3);
	}

	@Test
	@DisplayName("공지사항 상세 조회 성공")
	void detail_success() {
		Notice saved = noticeRepository.findAll().get(0);
		NoticeDetailResponse detail = noticeService.getNoticeDetail(saved.getId());

		assertThat(detail.title()).isEqualTo(saved.getTitle());
		assertThat(detail.content()).isEqualTo(saved.getContent());
		assertThat(detail.viewCount()).isEqualTo(1); // 조회 시 viewCount 증가
	}

	@Test
	@DisplayName("존재하지 않는 ID 조회 시 예외 발생")
	void detail_notFound() {
		Long notExistId = 9999L;
		assertThatThrownBy(() -> noticeService.getNoticeDetail(notExistId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("공지사항이 존재하지 않습니다");
	}

	@Test
	@DisplayName("제목+내용 검색 조건으로 조회")
	void list_with_keyword() {
		NoticeSearchCondition condition = new NoticeSearchCondition(
				"긴급",   // keyword
				false,  // titleOnly
				null,   // startDate
				null    // endDate
		);

		Page<NoticeSummaryResponse> result = noticeService.getNoticeList(condition,
				PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).title()).contains("업데이트");
	}

	@Test
	@DisplayName("제목만 검색 조건으로 조회")
	void list_with_titleOnly() {
		NoticeSearchCondition condition = new NoticeSearchCondition(
				"이벤트",   // keyword
				true,  // titleOnly
				null,   // startDate
				null    // endDate
		);

		Page<NoticeSummaryResponse> result = noticeService.getNoticeList(condition,
				PageRequest.of(0, 10));
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("등록일 범위 검색 조건으로 조회")
	void list_with_date_range() {
		Instant start = Instant.now().minus(1, ChronoUnit.DAYS);
		Instant end = Instant.now();

		NoticeSearchCondition condition = new NoticeSearchCondition(
				null,   // keyword
				false,  // titleOnly
				start,   // startDate
				end    // endDate
		);

		Page<NoticeSummaryResponse> result = noticeService.getNoticeList(condition,
				PageRequest.of(0, 10));

		assertThat(result.getTotalElements()).isEqualTo(3);
	}

	@Test
	@DisplayName("첨부파일이 있는 공지사항만 확인")
	void list_attachment_check() {
		NoticeSearchCondition condition = new NoticeSearchCondition(
				null,   // keyword
				false,  // titleOnly
				null,   // startDate
				null    // endDate
		);

		Page<NoticeSummaryResponse> result = noticeService.getNoticeList(condition,
				PageRequest.of(0, 10));
		NoticeSummaryResponse notice = result.getContent().stream()
				.filter(r -> r.title().contains("업데이트")).findFirst().orElseThrow();

		assertThat(notice.hasAttachment()).isTrue();
	}
}