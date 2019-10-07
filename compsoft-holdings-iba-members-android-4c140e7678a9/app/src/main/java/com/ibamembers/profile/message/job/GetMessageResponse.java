package com.ibamembers.profile.message.job;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class GetMessageResponse {

	@SuppressWarnings("unused")
	@SerializedName("ThreadId")
	private int threadId;

	@SuppressWarnings("unused")
	@SerializedName("Messages")
	private List<ProfileMessageModel> messages;

	@SuppressWarnings("unused")
	@SerializedName("RecipientId")
	private int recipientId;

	@SuppressWarnings("unused")
	@SerializedName("OtherParticipantLastSeenDateTime")
	private Date otherParticipantLastSeenDateTime;

	public int getThreadId() {
		return threadId;
	}

	public List<ProfileMessageModel> getMessages() {
		return messages;
	}

	public int getRecipientId() {
		return recipientId;
	}

	public Date getOtherParticipantLastSeenDateTime() {
		return otherParticipantLastSeenDateTime;
	}
}
