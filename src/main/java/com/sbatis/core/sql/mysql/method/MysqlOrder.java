package com.sbatis.core.sql.mysql.method;

import com.sbatis.core.constant.type.OrderEnum;
import com.sbatis.core.sql.AbstractOrder;

import java.util.List;

public class MysqlOrder extends AbstractOrder {

	@Override
	protected void addOrder(List<String[]> orders, String orderName, OrderEnum orderType) {
		String[] order = {"`" + orderName + "` ", (OrderEnum.ORDER_ASC.equals(orderType) ? "asc" : "desc")};
		orders.add(order);
	}

}
