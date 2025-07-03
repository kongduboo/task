package com.rsupport.task.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
@Slf4j
public class ApiLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {

		String method = request.getMethod();
		String uri = request.getRequestURI();
		String queryString = request.getQueryString();
		String fullUrl = uri + (queryString != null ? "?" + queryString : "");
		String clientIp = request.getRemoteAddr();

		log.info("Incoming API Request: [{}] {} from {}", method, fullUrl, clientIp);

		filterChain.doFilter(request, response);

		log.info("Completed API Request: [{}] {} with status={}",
				method, fullUrl, response.getStatus());
	}
}