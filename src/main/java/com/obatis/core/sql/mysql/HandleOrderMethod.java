package com.obatis.core.sql.mysql;

import com.obatis.core.constant.type.OrderEnum;
import com.obatis.core.sql.AbstractOrder;

import java.util.List;

/**
 * mysql 数据库排序实现
 * @author HuangLongPu
 */
public class HandleOrderMethod extends AbstractOrder {

	/**
	 * 实现 mysql 排序的 sql 语句构建
	 * @author HuangLongPu
	 * @param orders
	 * @param orderName
	 * @param orderType
	 */
	@Override
	protected void addOrder(List<String[]> orders, String orderName, OrderEnum orderType) {
		String[] order = {"`" + orderName + "` ", (OrderEnum.ORDER_ASC.equals(orderType) ? "asc" : "desc")};
		orders.add(order);
	}

}
