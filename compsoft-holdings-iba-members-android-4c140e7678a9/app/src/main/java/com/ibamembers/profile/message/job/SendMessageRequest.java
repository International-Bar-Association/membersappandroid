package com.ibamembers.profile.message.job;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {

	@SuppressWarnings("unused")
	@SerializedName("UserId")
	private int userId;

	@SuppressWarnings("unused")
	@SerializedName("Message")
	private String message;

	@SuppressWarnings("unused")
	@SerializedName("UUID")
	private String uuid;

	public SendMessageRequest(int userId, String uuid, String message) {
		this.userId = userId;
		this.uuid = uuid;
		this.message = message;
	}

	public int isUserId() {
		return userId;
	}

	public String getUuid() {
		return uuid;
	}

	public String getMessage() {
		return message;
	}
}
