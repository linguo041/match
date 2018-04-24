package com.roy.football.match.OFN.response;

import lombok.Data;

@Data
public class OFNResponseWrapper <T> {
	private T data;
	private String msg;
	private Integer code;
}
