package com.ibamembers.content.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ContentDownload {

    @DatabaseField(id = true, index = true, columnName = ContentDownloadDao.COLUMN_CONTENT_ID)
    private int id;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_THUMB_URL)
    private String thumbnailUrl;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_TITLE)
    private String title;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_PRECIS)
    private String precis;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_CONTENT_TYPE)
    private int contentType;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_URL)
    private String url;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_FEATURED)
    private boolean featured;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_CREATED)
    private String created;

    @DatabaseField(columnName = ContentDownloadDao.COLUMN_FILE_DIR)
    private String fileDir;

    @SuppressWarnings("unused")
    public ContentDownload() {}

    public ContentDownload(int id, String thumbnailUrl, String title, String precis, int contentType, String url, boolean featured, String created, String fileDir) {
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

    public int getContentType() {
        return contentType;
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

    public void setFileDir(String fileFDir) {
        this.fileDir = fileFDir;
    }

    public String getFileDir() {
        return fileDir;
    }

}
