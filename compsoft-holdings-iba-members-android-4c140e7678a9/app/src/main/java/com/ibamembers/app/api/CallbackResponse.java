package com.ibamembers.app.api;

import com.google.gson.annotations.SerializedName;

public class CallbackResponse {

    @SerializedName("Message")
    private String message;

    public String getSeMessage() {
        return message;
    }
}