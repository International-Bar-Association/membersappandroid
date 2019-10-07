package com.ibamembers.profile.message.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.messages.job.BaseMessageModel;

import java.util.Date;

public class ProfileMessageModel extends BaseMessageModel {

	@SuppressWarnings("unused")
	@SerializedName("MessageId")
	private int messageId;

	@SuppressWarnings("unused")
	@SerializedName("UUID")
	private String uuid;

	@SuppressWarnings("unused")
	@SerializedName("Message")
	private String message;

	@SuppressWarnings("unused")
	@SerializedName("SentByMe")
	private boolean sentMyBe;

	@SuppressWarnings("unused")
	@SerializedName("SentTime")
	private Date sentTime;

	@SuppressWarnings("unused")
	@SerializedName("DeliveredTime")
	private Date deliveredTime;

	@SuppressWarnings("unused")
	@SerializedName("ReadTime")
	private Date readTime;

	private MessageStatus messageStatus;

	public enum MessageStatus {
		Sending,
		Sent,
		Failed
	}

	public ProfileMessageModel(String uuid, String message, boolean sentMyBe, Date sentTime, Date deliveredTime, Date readTime, MessageStatus messageStatus) {
		this.uuid = uuid;
		this.message = message;
		this.sentMyBe = sentMyBe;
		this.sentTime = sentTime;
		this.deliveredTime = deliveredTime;
		this.readTime = readTime;
		this.messageStatus = messageStatus;
	}

	public int getMessageId() {
		return messageId;
	}

	public String getMessage() {
		return message;
	}

	public String getUuid() {
		return uuid;
	}

	public boolean isSentMyBe() {
		return sentMyBe;
	}

	public Date getSentTime() {
		return sentTime;
	}

	public Date getDeliveredTime() {
		return deliveredTime;
	}

	public Date getReadTime() {
		return readTime;
	}

	public MessageStatus getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(MessageStatus messageStatus) {
		this.messageStatus = messageStatus;
	}
}
