package com.rsupport.task.api.v1.notice.service;

import com.rsupport.task.api.v1.notice.domain.NoticeAttachment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoticeAttachmentServiceImpl implements NoticeAttachmentService {

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	public List<NoticeAttachment> storeFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}

		return files.stream()
				.map(file -> {
					String originalName = file.getOriginalFilename();
					String storedName = UUID.randomUUID() + "_" + originalName;
					String fullPath = uploadDir + "/" + storedName;

					try {
						file.transferTo(new File(fullPath));
					} catch (IOException e) {
						throw new RuntimeException("파일 저장 실패", e);
					}

					return NoticeAttachment.builder()
							.fileName(originalName)
							.filePath(fullPath)
							.build();
				})
				.toList();
	}
}
