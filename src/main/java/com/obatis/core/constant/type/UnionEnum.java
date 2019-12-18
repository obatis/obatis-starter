package com.obatis.core.constant.type;

public enum UnionEnum {

    /**
     * 表示 union 连接查询
     */
    UNION(" union "),
    /**
     * 表示union all 连接查询(默认)
     */
    UNION_ALL(" union all ");

    private String unionType;

    UnionEnum(String unionType) {
        this.unionType = unionType;
    }

    public String getUnionType() {
        return this.unionType;
    }
}
