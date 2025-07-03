package com.rsupport.task.api.v1.notice.controller;

import com.rsupport.task.api.v1.notice.dto.request.NoticeCreateRequest;
import com.rsupport.task.api.v1.notice.dto.request.NoticeSearchCondition;
import com.rsupport.task.api.v1.notice.dto.request.NoticeUpdateRequest;
import com.rsupport.task.api.v1.notice.dto.response.NoticeDetailResponse;
import com.rsupport.task.api.v1.notice.dto.response.NoticeSummaryResponse;
import com.rsupport.task.api.v1.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;

	@PostMapping
	public ResponseEntity<Long> registerNotice(
			@RequestPart("data") @Valid NoticeCreateRequest request,
			@RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
	) {
		Long id = noticeService.registerNotice(request, attachments);
		return ResponseEntity.ok(id);
	}

	@GetMapping
	public Page<NoticeSummaryResponse> list(
			@ModelAttribute NoticeSearchCondition condition,
			Pageable pageable
	) {
		return noticeService.getNoticeList(condition, pageable);
	}

	@GetMapping("/{id}")
	public NoticeDetailResponse detail(@PathVariable Long id) {
		return noticeService.getNoticeDetail(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> updateNotice(
			@PathVariable Long id,
			@RequestPart("data") @Valid NoticeUpdateRequest request,
			@RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
	) {
		noticeService.updateNotice(id, request, attachments);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
		noticeService.deleteNotice(id);
		return ResponseEntity.noContent().build();
	}
}
