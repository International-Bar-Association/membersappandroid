package com.ibamembers.messages.job;

import com.google.gson.annotations.SerializedName;

public class MessageStatusDeleted {

    @SerializedName("AppUserMessageId")
    private int appUserMessageId;

    @SerializedName("Deleted")
    private String deleted;

    public MessageStatusDeleted(int appUserMessageId, String deleted) {
        this.appUserMessageId = appUserMessageId;
        this.deleted = deleted;
    }
}
