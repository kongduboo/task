package com.rsupport.task.api.v1.notice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsupport.task.api.v1.notice.dto.request.NoticeCreateRequest;
import com.rsupport.task.api.v1.notice.dto.request.NoticeUpdateRequest;
import com.rsupport.task.api.v1.notice.service.NoticeService;
import com.rsupport.task.common.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoticeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private NoticeService noticeService;

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("공지사항등록_정상동작")
	@Test
	void 공지사항등록_정상동작() throws Exception {
		// given
		NoticeCreateRequest request = new NoticeCreateRequest(
				"테스트 공지사항 제목",
				"테스트 공지사항 내용",
				Instant.parse("2025-06-28T00:00:00Z"),
				Instant.parse("2025-07-28T00:00:00Z"),
				"관리자"
		);

		String json = objectMapper.writeValueAsString(request);

		MockMultipartFile dataPart = new MockMultipartFile(
				"data", "data", "application/json", json.getBytes(StandardCharsets.UTF_8)
		);

		MockMultipartFile file = new MockMultipartFile(
				"attachments", "test.txt", "text/plain", "파일내용".getBytes()
		);

		// when & then
		mockMvc.perform(multipart("/api/v1/notices")
						.file(dataPart)
						.file(file)
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNumber());
	}

	@DisplayName("공지사항등록_제목없음_실패")
	@Test
	void 공지사항등록_제목없음_실패() throws Exception {
		// given
		NoticeCreateRequest request = new NoticeCreateRequest(
				"",  // 제목 없음
				"내용",
				Instant.now(),
				Instant.now().plusSeconds(3600),
				"작성자"
		);

		String json = objectMapper.writeValueAsString(request);
		MockMultipartFile dataPart = new MockMultipartFile("data", "data", "application/json",
				json.getBytes());

		// when & then
		mockMvc.perform(multipart("/api/v1/notices")
						.file(dataPart)
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("공지사항등록_실패_시작일이_종료일보다_늦음")
	void 공지사항등록_실패_시작일이_종료일보다_늦음() throws Exception {
		// given
		NoticeCreateRequest invalidRequest = new NoticeCreateRequest(
				"잘못된 기간 테스트",
				"내용입니다.",
				Instant.parse("2025-07-01T12:00:00Z"), // startAt
				Instant.parse("2025-06-30T12:00:00Z"), // endAt (더 이전)
				"작성자"
		);

		MockMultipartFile data = new MockMultipartFile(
				"data",
				null,
				MediaType.APPLICATION_JSON_VALUE,
				objectMapper.writeValueAsBytes(invalidRequest)
		);

		// when & then
		mockMvc.perform(multipart("/api/v1/notices")
						.file(data)
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
				.andExpect(jsonPath("$.errors.validPeriod").value("시작일은 종료일보다 이전이어야 합니다."));
	}

	@Test
	void 공지사항_등록_첨부파일_2개() throws Exception {
		// given
		NoticeCreateRequest request = new NoticeCreateRequest(
				"다중 첨부파일 테스트",
				"파일 2개 업로드",
				Instant.parse("2025-06-28T10:00:00Z"),
				Instant.parse("2025-06-30T10:00:00Z"),
				"관리자"
		);

		MockMultipartFile jsonPart = new MockMultipartFile(
				"data",
				null,
				"application/json",
				objectMapper.writeValueAsBytes(request)
		);

		MockMultipartFile file1 = new MockMultipartFile(
				"attachments",
				"test.txt",
				"text/plain",
				"첫번째 파일입니다.".getBytes()
		);

		MockMultipartFile file2 = new MockMultipartFile(
				"attachments",
				"test2.txt",
				"text/plain",
				"두번째 파일입니다.".getBytes()
		);

		// when & then
		mockMvc.perform(multipart("/api/v1/notices")
						.file(jsonPart)
						.file(file1)
						.file(file2)
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNumber());
	}

	@Test
	@DisplayName("공지사항 수정 성공 - 첨부파일 포함")
	void update_notice_with_file_success() throws Exception {
		Long noticeId = 1L;

		NoticeUpdateRequest request = new NoticeUpdateRequest(
				"수정된 제목",
				"수정된 내용",
				Instant.parse("2025-06-01T00:00:00Z"),
				Instant.parse("2025-07-10T00:00:00Z"),
				"홍길동"
		);
		MockMultipartFile jsonPart = new MockMultipartFile(
				"data",
				"",
				"application/json",
				objectMapper.writeValueAsBytes(request)
		);

		// 파일 파트
		MockMultipartFile filePart = new MockMultipartFile(
				"attachments",
				"updated.txt",
				"text/plain",
				"파일 내용입니다.".getBytes(StandardCharsets.UTF_8)
		);

		mockMvc.perform(multipart("/api/v1/notices/{id}", noticeId)
						.file(jsonPart)
						.file(filePart)
						.with(req -> {
							req.setMethod("PUT"); // multipart 기본은 POST이므로 PUT으로 강제
							return req;
						}))
				.andExpect(status().isNoContent());

		// verify 호출 검증도 가능
		verify(noticeService).updateNotice(eq(noticeId), any(), any());
	}

	@Test
	@DisplayName("공지사항 수정 성공 - 첨부파일 없음")
	void update_notice_without_file_success() throws Exception {
		Long noticeId = 2L;

		NoticeUpdateRequest request = new NoticeUpdateRequest(
				"파일 없는 수정",
				"본문만 수정",
				Instant.parse("2025-06-05T00:00:00Z"),
				Instant.parse("2025-07-12T00:00:00Z"),
				"관리자"
		);
		MockMultipartFile jsonPart = new MockMultipartFile(
				"data",
				"",
				"application/json",
				objectMapper.writeValueAsBytes(request)
		);

		mockMvc.perform(multipart("/api/v1/notices/{id}", noticeId)
						.file(jsonPart)
						.with(req -> {
							req.setMethod("PUT");
							return req;
						}))
				.andExpect(status().isNoContent());

		verify(noticeService).updateNotice(eq(noticeId), any(), isNull());
	}

	@Test
	@DisplayName("공지사항 수정 실패 - 존재하지 않는 공지")
	void update_notice_not_found() throws Exception {
		Long invalidId = 999L;

		NoticeUpdateRequest request = new NoticeUpdateRequest(
				"없는 공지",
				"존재하지 않음",
				Instant.now(),
				Instant.now().plusSeconds(3600),
				"사용자"
		);
		MockMultipartFile jsonPart = new MockMultipartFile(
				"data",
				"",
				"application/json",
				objectMapper.writeValueAsBytes(request)
		);

		doThrow(new NotFoundException("공지사항을 찾을 수 없습니다."))
				.when(noticeService)
				.updateNotice(eq(invalidId), any(), any());

		mockMvc.perform(multipart("/api/v1/notices/{id}", invalidId)
						.file(jsonPart)
						.with(req -> {
							req.setMethod("PUT");
							return req;
						}))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("공지사항을 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("공지사항 삭제 성공")
	void delete_notice_success() throws Exception {
		mockMvc.perform(delete("/api/v1/notices/{id}", 1L))
				.andExpect(status().isNoContent());

		verify(noticeService).deleteNotice(1L);
	}

	@Test
	@DisplayName("공지사항 삭제 실패 - 존재하지 않는 ID")
	void delete_notice_not_found() throws Exception {
		doThrow(new NotFoundException("공지사항을 찾을 수 없습니다."))
				.when(noticeService).deleteNotice(999L);

		mockMvc.perform(delete("/api/v1/notices/{id}", 999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("공지사항을 찾을 수 없습니다."));
	}

	@AfterEach
	void cleanup() {
		File dir = new File("uploads/");
		if (dir.exists()) {
//			for (File file : dir.listFiles()) {
//				file.delete();
//			}
//			dir.delete();
		}
	}
}