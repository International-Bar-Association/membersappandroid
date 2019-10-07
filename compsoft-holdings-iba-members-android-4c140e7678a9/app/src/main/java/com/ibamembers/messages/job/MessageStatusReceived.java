package com.ibamembers.messages.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.messages.MessagesDetailFragment;

public class MessageStatusReceived {

    @SerializedName("AppUserMessageId")
    private int appUserMessageId;

    @SerializedName("Received")
    private String received;

    public MessageStatusReceived(int appUserMessageId, String received) {
        this.appUserMessageId = appUserMessageId;
        this.received = received;
    }
}
