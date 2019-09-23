package com.obatis.core.sql;

import com.obatis.core.exception.HandleException;
import com.obatis.validate.ValidateTool;

public class QueryProviderExpHandle {

    private StringBuffer exp = new StringBuffer();

    public String toString() {
        return exp.toString();
    }

    public QueryProviderExpHandle add(String...columns) {
        return handleColumn("+", columns);
    }

    public QueryProviderExpHandle sub(String...columns) {
        return handleColumn("-", columns);
    }

    public QueryProviderExpHandle multiply(String...columns) {
        return handleColumn("*", columns);
    }

    public QueryProviderExpHandle divide(String...columns) {
        return handleColumn("/", columns);
    }

    private QueryProviderExpHandle handleColumn(String operator, String...columns) {
        if(columns == null) {
            throw new HandleException("error: columns is null");
        }

        if(!ValidateTool.isEmpty(exp.toString())) {
//            exp.insert(0, "(");
//            exp.append(")");
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
