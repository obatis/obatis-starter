package com.sbatis.core.sql;

import com.sbatis.core.constant.type.OrderEnum;

import java.util.List;

public abstract class AbstractOrder {

	protected AbstractOrder() {

	}
	
	protected abstract void addOrder(List<String[]> orders, String orderName, OrderEnum orderType);
}
