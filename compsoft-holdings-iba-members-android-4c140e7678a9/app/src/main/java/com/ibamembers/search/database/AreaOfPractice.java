package com.ibamembers.search.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class AreaOfPractice {

    @DatabaseField(id = true, index = true, columnName = AreaOfPracticeDao.COLUMN_ID)
    private long id;

    @DatabaseField(columnName = AreaOfPracticeDao.COLUMN_NAME)
    private String name;

    @SuppressWarnings("unused")
    public AreaOfPractice() {}

    public AreaOfPractice(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
