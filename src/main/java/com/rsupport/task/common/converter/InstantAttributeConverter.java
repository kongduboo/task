package com.rsupport.task.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.Instant;

@Converter(autoApply = true)
public class InstantAttributeConverter implements AttributeConverter<Instant, Timestamp> {

	@Override
	public Timestamp convertToDatabaseColumn(Instant instant) {
		return instant != null ? Timestamp.from(instant) : null;
	}

	@Override
	public Instant convertToEntityAttribute(Timestamp timestamp) {
		return timestamp != null ? timestamp.toInstant() : null;
	}
}
