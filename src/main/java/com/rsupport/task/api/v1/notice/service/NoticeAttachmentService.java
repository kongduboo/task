package com.rsupport.task.api.v1.notice.service;

import com.rsupport.task.api.v1.notice.domain.NoticeAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoticeAttachmentService {

	List<NoticeAttachment> storeFiles(List<MultipartFile> files);
}
