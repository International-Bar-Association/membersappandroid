package com.ibamembers.profile.message.job;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class SendMessageResponse {

	@SuppressWarnings("unused")
	@SerializedName("Success")
	private boolean success;

	@SuppressWarnings("unused")
	@SerializedName("Message")
	private Message message;

	public boolean isSuccess() {
		return success;
	}

	public Message getMessage() {
		return message;
	}

	public class Message {

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
		private boolean sentByMe;

		@SuppressWarnings("unused")
		@SerializedName("SentTime")
		private Date sentTime;

		@SuppressWarnings("unused")
		@SerializedName("DeliveredTime")
		private Date deliveredTime;

		@SuppressWarnings("unused")
		@SerializedName("ReadTime")
		private Date readTime;

		public int getMessageId() {
			return messageId;
		}

		public String getMessage() {
			return message;
		}


		public String getUuid() {
			return uuid;
		}

		public boolean isSentByMe() {
			return sentByMe;
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
	}
}
