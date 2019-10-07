package com.ibamembers.app;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable
public class Setting {

    @DatabaseField(id = true, columnName = SettingDao.COLUMN_ID)
    private long id;

    @DatabaseField(columnName = SettingDao.COLUMN_VALUE_STRING)
    private String valueString;

    @DatabaseField(columnName = SettingDao.COLUMN_VALUE_DATE)
    private Date valueDate;

    @DatabaseField(columnName = SettingDao.COLUMN_VALUE_FLOAT)
    private float valueFloat;

    @DatabaseField(columnName = SettingDao.COLUMN_VALUE_BOOLEAN)
    private boolean valueBoolean;

    @DatabaseField(columnName = SettingDao.COLUMN_VALUE_INT)
    private int valueInt;

    @DatabaseField(columnName = SettingDao.COLUMN_VALUE_LONG)
    private long valueLong;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = SettingDao.COLUMN_VALUE_BYTE_ARRAY)
    private byte[] valueByteArray;

    @SuppressWarnings("unused")
    Setting() {}

    Setting(long id, String value) {
        this.id = id;
        this.valueString = value;
    }

    Setting(long id, Date value) {
        this.id = id;
        this.valueDate = value;
    }

    Setting(long id, float value) {
        this.id = id;
        this.valueFloat = value;
    }

    Setting(long id, boolean value) {
        this.id = id;
        this.valueBoolean = value;
    }

    Setting(long id, int value) {
        this.id = id;
        this.valueInt = value;
    }

    Setting(long id, long value) {
        this.id = id;
        this.valueLong = value;
    }

    Setting(long id, byte[] value) {
        this.id = id;
        this.valueByteArray = value;
    }

    public long getId() {
        return id;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public void setValueDate(Date valueDate) {
        this.valueDate = valueDate;
    }

    public float getValueFloat() {
        return valueFloat;
    }

    public void setValueFloat(float valueFloat) {
        this.valueFloat = valueFloat;
    }

    public boolean isValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public int getValueInt() {
        return valueInt;
    }

    public void setValueInt(int valueInt) {
        this.valueInt = valueInt;
    }

    public long getValueLong() {
        return valueLong;
    }

    public void setValueLong(long valueLong) {
        this.valueLong = valueLong;
    }

    public byte[] getValueByteArray() {
        return valueByteArray;
    }

    public void setValueByteArray(byte[] valueByteArray) {
        this.valueByteArray = valueByteArray;
    }
}
