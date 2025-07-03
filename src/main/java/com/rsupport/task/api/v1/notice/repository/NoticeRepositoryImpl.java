package com.rsupport.task.api.v1.notice.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.rsupport.task.api.v1.notice.domain.Notice;
import com.rsupport.task.api.v1.notice.domain.QNotice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Page<Notice> search(String keyword, boolean titleOnly, Instant startDate,
			Instant endDate, Pageable pageable) {
		QNotice notice = QNotice.notice;

		BooleanBuilder builder = new BooleanBuilder();

		if (StringUtils.hasText(keyword)) {
			if (titleOnly) {
				builder.and(notice.title.containsIgnoreCase(keyword));
			} else {
				builder.and(notice.title.containsIgnoreCase(keyword)
						.or(notice.content.containsIgnoreCase(keyword)));
			}
		}

		if (startDate != null) {
			builder.and(notice.createdAt.goe(startDate));
		}
		if (endDate != null) {
			builder.and(notice.createdAt.loe(endDate));
		}

		// 목록 쿼리: 정렬 포함
		List<Notice> content = new JPAQuery<>(em)
				.select(notice)
				.from(notice)
				.where(builder)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.orderBy(notice.createdAt.desc())
				.fetch();

		// 카운트 쿼리: 정렬 제거
		long count = new JPAQuery<>(em)
				.select(notice.count())
				.from(notice)
				.where(builder)
				.fetchOne();

		return new PageImpl<>(content, pageable, count);
	}
}
