package com.obatis.core.constant.type;

/**
 * 查询条件 SQL 表达式枚举
 * @author HuangLongPu
 */
public enum FilterEnum {

	/**
	 * 模糊查询，like ==> '%?%'
	 */
	LIKE,
	/**
	 * 左模糊查询，like ==> '%?'
	 */
	LEFT_LIKE,
	/**
	 * 右模糊查询，like ==> '?%'
	 */
	RIGHT_LIKE,
	/**
	 * 等于，=
	 */
	EQUAL,
	/**
	 * 大于，>
	 */
	GREATER_THAN,
	/**
	 * 大于等于，>=
	 */
	GREATER_EQUAL,
	/**
	 * 小于，<
	 */
	LESS_THAN,
	/**
	 * 小于等于，<=
	 */
	LESS_EQUAL,
	/**
	 * 不等于，<>
	 */
	NOT_EQUAL,
	/**
	 * "in" 查询，in
	 */
	IN,
	/**
	 * "not in" 查询，not in
	 */
	NOT_IN,
	/**
	 * 空值(null)，is null
	 */
	IS_NULL,
	/**
	 * 不为空值(null)，is not null
	 */
	IS_NOT_NULL,
	/**
	 * 表达式 "加" 运算大于条件判断，比如 total + 10 > 0
	 */
	UP_GREATER_THAN,
	/**
	 * 表达式 "加" 运算大于等于条件判断，比如 total + 10 >= 0
	 */
	UP_GREATER_EQUAL,
	/**
	 * 表达式 "减" 运算小于条件判断，比如 total - 10 > 0
	 */
	REDUCE_GREATER_THAN,
	/**
	 * 表达式 "减" 运算小于等于条件判断，比如 total - 10 >= 0
	 */
	REDUCE_GREATER_EQUAL
}
