package com.ibamembers.messages.job;

import com.google.gson.annotations.SerializedName;

public class MessageStatusRead {

    @SerializedName("AppUserMessageId")
    private int appUserMessageId;

    @SerializedName("Read")
    private String read;

    public MessageStatusRead(int appUserMessageId, String read) {
        this.appUserMessageId = appUserMessageId;
        this.read = read;
    }
}
