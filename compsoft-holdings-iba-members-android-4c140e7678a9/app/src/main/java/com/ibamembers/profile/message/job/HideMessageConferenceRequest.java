package com.ibamembers.profile.message.job;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HideMessageConferenceRequest {

	@SuppressWarnings("unused")
	@SerializedName("threadId")
	private int profileId;

	public HideMessageConferenceRequest(int profileId) {
		this.profileId = profileId;
	}
}
