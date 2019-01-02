package com.roy.football.match.fivemillion;

import lombok.Data;

@Data
public class FMResponseWrapper<T> {

	private String status;
	private String message;
	private T data;
}
