package com.obatis.core.constant.type;

public enum JoinTypeEnum {

    /**
     * 表示连接类型为 and，默认
     */
    AND(" and "),
    /**
     * 表示连接类型为 or
     */
    OR (" or ");

    private String joinTypeName;

    JoinTypeEnum(String joinTypeName) {
        this.joinTypeName = joinTypeName;
    }

    public String getJoinTypeName() {
        return this.joinTypeName;
    }
}
