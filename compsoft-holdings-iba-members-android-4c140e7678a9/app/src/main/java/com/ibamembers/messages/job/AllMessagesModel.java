package com.ibamembers.messages.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.app.api.ResponseError;

import java.util.List;

public class AllMessagesModel {

    @SerializedName("Messages")
    private List<GeneralMessageModel> messages;

    @SerializedName("TotalRecords")
    private int totalRecords;

    @SerializedName("ResponseError")
    private ResponseError responseError;

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setMessages(List<GeneralMessageModel> messages) {
        this.messages = messages;
    }

    public List<GeneralMessageModel> getMessages() {
        return messages;
    }

    public ResponseError getResponseError() {
        return responseError;
    }
}
