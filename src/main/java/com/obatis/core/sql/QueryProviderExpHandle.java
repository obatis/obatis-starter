package com.obatis.core.sql;

import com.obatis.core.exception.HandleException;
import com.obatis.tools.ValidateTool;

public class QueryProviderExpHandle {

    private StringBuffer exp = new StringBuffer();

    public String toString() {
        return exp.toString();
    }

    /**
     * 主要提供将表达式结果为null时，转化为0返回
     * @return
     */
    public QueryProviderExpHandle nullToZero() {
        exp.insert(0, "IFNULL((");
        exp.append("), 0)");
        return this;
    }

    /**
     * 主要提供字段间相加的表达式，例如：field1 + field2 + field3
     * @param columns
     * @return
     */
    public QueryProviderExpHandle add(String...columns) {
        return handleColumn("+", columns);
    }

    /**
     * 主要提供字段间相减的表达式，例如：field1 - field2 - field3
     * @param columns
     * @return
     */
    public QueryProviderExpHandle sub(String...columns) {
        return handleColumn("-", columns);
    }

    /**
     * 主要提供字段间相乘的表达式，例如：field1 * field2 * field3
     * @param columns
     * @return
     */
    public QueryProviderExpHandle multiply(String...columns) {
        return handleColumn("*", columns);
    }

    /**
     * 主要提供字段间相除的表达式，例如：field1 / field2 / field3
     * @param columns
     * @return
     */
    public QueryProviderExpHandle divide(String...columns) {
        return handleColumn("/", columns);
    }

    private QueryProviderExpHandle handleColumn(String operator, String...columns) {
        if(columns == null) {
            throw new HandleException("error: columns is null");
        }

        if(!ValidateTool.isEmpty(exp.toString())) {
            exp.append(operator);
        }

        boolean itemAppendFlag = false;
        int columnLength = columns.length;
        if(columns.length > 1) {
            itemAppendFlag = true;
            exp.append("(");
        }

        for (int i = 0; i < columnLength; i++) {
            exp.append(columns[i]);
            if(i != columnLength - 1) {
                exp.append(operator);
            }
        }

        if(itemAppendFlag) {
            exp.append(")");
        }

        return this;
    }

}
