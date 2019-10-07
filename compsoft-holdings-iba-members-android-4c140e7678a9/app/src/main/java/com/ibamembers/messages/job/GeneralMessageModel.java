package com.ibamembers.messages.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.messages.MessagesDetailFragment;

import java.util.Date;

public class GeneralMessageModel extends BaseMessageModel {

    @SerializedName("Date")
    private Date date;

    @SerializedName("AppUserMessageId")
    private int appUserMessageId;

    @SerializedName("MessageType")
    private int messageType;

    @SerializedName("Title")
    private String title;

    @SerializedName("Text")
    private String text;

    @SerializedName("Url")
    private String url;

    @SerializedName("Status")
    private int status;

    public GeneralMessageModel(int appUserMessageId, Date date, int messageType, String title, String text, String url, int status) {
        this.appUserMessageId = appUserMessageId;
        this.date = date;
        this.messageType = messageType;
        this.title = title;
        this.text = text;
        this.url = url;
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public MessagesDetailFragment.MessageStatus getStatus() {
        return MessagesDetailFragment.MessageStatus.forInt(status);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAppUserMessageId() {
        return appUserMessageId;
    }

    public MessagesDetailFragment.MessageType getMessageType() {
        return MessagesDetailFragment.MessageType.forInt(messageType);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
