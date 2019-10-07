package com.ibamembers.search.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Committee {

    @DatabaseField(id = true, index = true, columnName = CommitteeDao.COLUMN_ID)
    private long id;

    @DatabaseField(columnName = CommitteeDao.COLUMN_NAME)
    private String name;

    @SuppressWarnings("unused")
    public Committee() {}

    public Committee(long id, String name) {
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