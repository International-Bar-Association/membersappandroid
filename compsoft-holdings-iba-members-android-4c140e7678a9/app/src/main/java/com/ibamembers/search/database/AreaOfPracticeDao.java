package com.ibamembers.search.database;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class AreaOfPracticeDao extends BaseDaoImpl<AreaOfPractice, Long> {

    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";

    public AreaOfPracticeDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, AreaOfPractice.class);
    }
}
