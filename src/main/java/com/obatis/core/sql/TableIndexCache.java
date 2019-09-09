package com.obatis.core.sql;

public class TableIndexCache {

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTableAsName() {
        setIndex(index + 1);
        return "t_" + getIndex();
    }
}
