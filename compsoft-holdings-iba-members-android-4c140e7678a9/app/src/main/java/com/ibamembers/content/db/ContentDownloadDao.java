package com.ibamembers.content.db;

import com.ibamembers.content.job.ContentModel;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class ContentDownloadDao extends BaseDaoImpl<ContentDownload, Integer> {

    static final String COLUMN_CONTENT_ID = "contentId";
    static final String COLUMN_THUMB_URL = "thumbnailUrl";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_PRECIS = "precis";
    static final String COLUMN_CONTENT_TYPE = "contentType";
    static final String COLUMN_URL = "url";
    static final String COLUMN_FEATURED = "featured";
    static final String COLUMN_CREATED = "created";
    static final String COLUMN_FILE_DIR = "fileDir";

    public ContentDownloadDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ContentDownload.class);
    }

    public void removeContentFromDownloads(int contentId) throws SQLException {
        ContentDownload contentDownloadModel = queryForId(contentId);
        if (contentDownloadModel != null) {
            delete(contentDownloadModel);
        }
    }

    public void saveContentToDownloads(ContentModel contentModel) throws SQLException {
        ContentDownload contentDownload = new ContentDownload(contentModel.getId(),
                contentModel.getThumbnailUrl(),
                contentModel.getTitle(),
                contentModel.getPrecis(),
                contentModel.getContentType().ordinal(),
                contentModel.getUrl(),
                contentModel.isFeatured(),
                contentModel.getCreated(),
                contentModel.getFileDir());

        createOrUpdate(contentDownload);
    }

    public static ContentModel convertContentDownloadToContentModel(ContentDownload contentDownload) {
        return new ContentModel(contentDownload.getId(),
                contentDownload.getThumbnailUrl(),
                contentDownload.getTitle(),
                contentDownload.getPrecis(),
                contentDownload.getContentType(),
                contentDownload.getUrl(),
                contentDownload.isFeatured(),
                contentDownload.getCreated(),
                contentDownload.getFileDir());
    }
}
