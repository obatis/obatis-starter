package com.obatis.core.sql;

import com.obatis.core.constant.type.OrderEnum;
import com.obatis.core.constant.type.SqlHandleEnum;

import java.util.List;

public abstract class AbstractOrder {

	protected AbstractOrder() {

	}
	
	protected abstract void addOrder(List<Object[]> orders, String orderName, OrderEnum orderType, SqlHandleEnum sqlHandleEnum);
}
