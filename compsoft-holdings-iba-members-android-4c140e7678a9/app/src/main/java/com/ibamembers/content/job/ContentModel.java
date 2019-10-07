package com.ibamembers.content.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.content.ContentBaseFragment;

public class ContentModel {

    @SerializedName("Id")
    private int id;

    @SerializedName("ThumbnailUrl")
    private String thumbnailUrl;

    @SerializedName("Title")
    private String title;

    @SerializedName("Precis")
    private String precis;

    @SerializedName("ContentType")
    private int contentType;

    @SerializedName("Url")
    private String url;

    @SerializedName("Featured")
    private boolean featured;

    @SerializedName("Created")
    private String created;

    @SerializedName("FileDir")
    private String fileDir;

    public ContentModel(int id, String thumbnailUrl, String title, String precis, int contentType, String url, boolean featured, String created, String fileDir) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.precis = precis;
        this.contentType = contentType;
        this.url = url;
        this.featured = featured;
        this.created = created;
        this.fileDir = fileDir;
    }

    public int getId() {
        return id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getPrecis() {
        return precis;
    }

    public ContentBaseFragment.ContentLibraryType getContentType() {
        return ContentBaseFragment.ContentLibraryType.forInt(contentType);
    }

    public String getUrl() {
        return url;
    }

    public boolean isFeatured() {
        return featured;
    }

    public String getCreated() {
        return created;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getFileDir() {
        return fileDir;
    }
}
