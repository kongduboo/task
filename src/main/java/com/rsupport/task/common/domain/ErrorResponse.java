package com.rsupport.task.common.domain;

import com.rsupport.task.common.enums.ErrorCode;

public record ErrorResponse(String code, String message) {

//	public static ErrorResponse of(ErrorCode errorCode) {
//		return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
//	}
}
