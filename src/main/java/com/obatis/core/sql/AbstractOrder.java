package com.obatis.core.sql;

import com.obatis.core.constant.type.OrderEnum;

import java.util.List;

public abstract class AbstractOrder {

	protected AbstractOrder() {

	}
	
	protected abstract void addOrder(List<String[]> orders, String orderName, OrderEnum orderType);
}
