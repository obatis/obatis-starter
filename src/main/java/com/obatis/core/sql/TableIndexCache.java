package com.obatis.core.sql;

import java.util.HashMap;
import java.util.Map;

public class TableIndexCache {

    protected TableIndexCache() {

    }

    private Map<String, String> tableAsNameMap = new HashMap<>();

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTableAsName(String tableAsNameSerialNumber) {

        if(tableAsNameMap.containsKey(tableAsNameSerialNumber)) {
            return tableAsNameMap.get(tableAsNameSerialNumber);
        }

        setIndex(index + 1);

        String tableAsName = "t_" + getIndex();
        tableAsNameMap.put(tableAsNameSerialNumber, tableAsName);
        return tableAsName;
    }
}
