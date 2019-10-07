package com.ibamembers.content.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.app.api.ResponseError;

import java.util.List;

public class ContentLibraryModel {

    @SerializedName("Items")
    private List<ContentModel> contentList;

    @SerializedName("TotalRecords")
    private int totalRecords;

    @SerializedName("ResponseError")
    private ResponseError responseError;

    public List<ContentModel> getContentList() {
        return contentList;
    }

    public void setContentList(List<ContentModel> contentList) {
        this.contentList = contentList;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public ResponseError getResponseError() {
        return responseError;
    }
}
