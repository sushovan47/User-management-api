package com.demo.practice.model;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Response {
	private long id = increment();
	private String message = "";
	private boolean iSuccess = false;
	@JsonInclude(Include.NON_NULL)
	private Optional<List<?>> data;

	public static long increment() {
		AtomicInteger count = new AtomicInteger();
		return count.incrementAndGet();
	}
}
