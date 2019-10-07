package com.ibamembers.app;

public class DataItem {

    private long id;
    private String name;
    private boolean selected;

    public DataItem(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
    }

    public DataItem getCopy() {
        DataItem dataItem = new DataItem(id, name);
        dataItem.setSelected(selected);
        return dataItem;
    }
}
