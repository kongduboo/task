package com.rsupport.task.api.v1.notice.repository;

import com.rsupport.task.api.v1.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

}
