package com.ibamembers.messages.db;

import android.content.Context;

import com.ibamembers.R;
import com.ibamembers.messages.job.GeneralMessageModel;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewMessageBufferDao extends BaseDaoImpl<NewMessage, Integer> {

    static final String COLUMN_DATE = "date";
    static final String COLUMN_MESSAGE_ID = "messageId";
    static final String COLUMN_TYPE = "type";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_TEXT = "text";
    static final String COLUMN_URL = "url";
    static final String COLUMN_STATUS = "status";

    public NewMessageBufferDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, NewMessage.class);
    }

    public void removeNewMessageFromDownloads(int messageId) throws SQLException {
        NewMessage newMessage = queryForId(messageId);
        if (newMessage != null) {
            delete(newMessage);
        }
    }

    public void saveMessageModelAsNewMessage(Context context, GeneralMessageModel messageModel) throws SQLException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.default_api_date_format));
        String dateString = dateFormat.format(messageModel.getDate());

        NewMessage newMessage = new NewMessage(messageModel.getAppUserMessageId(),
                dateString,
                messageModel.getMessageType().ordinal(),
                messageModel.getTitle(),
                messageModel.getText(),
                messageModel.getUrl(),
                messageModel.getStatus().ordinal());

        createOrUpdate(newMessage);
    }

    public static GeneralMessageModel convertNewMessageToMessageModel(Context context, NewMessage newMessage) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.default_api_date_format));
        Date messageDate = dateFormat.parse(newMessage.getDate());

        return new GeneralMessageModel(newMessage.getId(),
                messageDate,
                newMessage.getType(),
                newMessage.getTitle(),
                newMessage.getText(),
                newMessage.getUrl(),
                newMessage.getStatus());
    }
}
