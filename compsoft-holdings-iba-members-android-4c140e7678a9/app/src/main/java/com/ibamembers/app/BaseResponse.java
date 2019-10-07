package com.ibamembers.app;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {

	@SuppressWarnings("unused")
	@SerializedName("Message")
	private String message;

	public String getMessage() {
		return message;
	}
}
