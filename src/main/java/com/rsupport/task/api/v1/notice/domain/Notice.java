package com.rsupport.task.api.v1.notice.domain;

import com.rsupport.task.common.converter.InstantAttributeConverter;
import com.rsupport.task.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notice", indexes = {
		@Index(name = "idx_notice_title", columnList = "title"),
		@Index(name = "idx_notice_content", columnList = "content"),
		@Index(name = "idx_notice_created_at", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Convert(converter = InstantAttributeConverter.class) // JPA 사용시 필요
	private Instant startAt;

	@Convert(converter = InstantAttributeConverter.class)
	private Instant endAt;

	private int viewCount;

	private String writer;

	@OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<NoticeAttachment> attachments = new ArrayList<>();

	public void addAttachment(NoticeAttachment attachment) {
		attachment.setNotice(this);
		attachments.add(attachment);
	}

	public void increaseViewCount(int delta) {
		this.viewCount += delta;
	}

	public void update(String title, String content, Instant startAt, Instant endAt,
			String writer) {
		this.title = title;
		this.content = content;
		this.startAt = startAt;
		this.endAt = endAt;
		this.writer = writer;
	}

	public void clearAttachments() {
		attachments.forEach(a -> a.setNotice(null));
		attachments.clear();
	}
}
