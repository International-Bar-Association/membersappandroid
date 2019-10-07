package com.ibamembers.messages.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class NewMessage {

    @DatabaseField(id = true, index = true, columnName = NewMessageBufferDao.COLUMN_MESSAGE_ID)
    private int id;

    @DatabaseField(columnName = NewMessageBufferDao.COLUMN_DATE)
    private String date;

    @DatabaseField(columnName = NewMessageBufferDao.COLUMN_TYPE)
    private int type;

    @DatabaseField(columnName = NewMessageBufferDao.COLUMN_TITLE)
    private String title;

    @DatabaseField(columnName = NewMessageBufferDao.COLUMN_TEXT)
    private String text;

    @DatabaseField(columnName = NewMessageBufferDao.COLUMN_URL)
    private String url;

    @DatabaseField(columnName = NewMessageBufferDao.COLUMN_STATUS)
    private int status;

    @SuppressWarnings("unused")
    public NewMessage() {}

    public NewMessage(int id, String date, int type, String title, String text, String url, int status) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.title = title;
        this.text = text;
        this.url = url;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }
}
