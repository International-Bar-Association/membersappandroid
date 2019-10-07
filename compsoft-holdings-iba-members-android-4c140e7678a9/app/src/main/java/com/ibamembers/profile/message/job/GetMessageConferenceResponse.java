package com.ibamembers.profile.message.job;

import com.google.gson.annotations.SerializedName;

public class GetMessageConferenceResponse {

	@SuppressWarnings("unused")
	@SerializedName("UserId")
	private int userId;

	@SuppressWarnings("unused")
	@SerializedName("UserProfileImageUrl")
	private String userProfileImageUrl;

	@SuppressWarnings("unused")
	@SerializedName("Name")
	private String name;

	@SuppressWarnings("unused")
	@SerializedName("LastMessage")
	private ProfileMessageModel lastMessage;

	public int getUserId() {
		return userId;
	}

	public String getUserProfileImageUrl() {
		return userProfileImageUrl;
	}

	public String getName() {
		return name;
	}

	public ProfileMessageModel getLastMessage() {
		return lastMessage;
	}
}