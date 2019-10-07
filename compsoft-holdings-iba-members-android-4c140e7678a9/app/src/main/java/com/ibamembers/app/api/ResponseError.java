package com.ibamembers.app.api;

import com.google.gson.annotations.SerializedName;

public class ResponseError {

    @SerializedName("Code")
    private int code;

    @SerializedName("Message")
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
