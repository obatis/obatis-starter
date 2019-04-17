package com.obatis.core.constant.type;

/**
 * 用于更新操作，操作类型枚举
 * @author HuangLongPu
 */
public enum SqlHandleEnum {

	/**
	 * 默认操作类型
	 */
	HANDLE_DEFAULT,
	/**
	 * 累加，例如 count = count + 10
	 */
	HANDLE_UP,
	/**
	 * 累减，例如 count = count - 10
	 */
	HANDLE_REDUCE,
	/**
	 * count计数统计
	 */
	HANDLE_COUNT,
	/**
	 * sum 求和
	 */
	HANDLE_SUM,
	/**
	 * 最小值
	 */
	HANDLE_MIN,
	/**
	 * 最大值
	 */
	HANDLE_MAX,
	/**
	 * 平均值
	 */
	HANDLE_AVG,
	/**
	 * 运算表达式
	 */
	HANDLE_EXP;
}
