package com.sbatis.core.constant.type;

/**
 * 查询条件 SQL 表达式枚举
 * @author HuangLongPu
 */
public enum FilterEnum {

	/**
	 * 表示全模糊查询，like
	 */
	FILTER_LIKE,
	/**
	 * 表示等于，=
	 */
	FILTER_EQUAL,
	/**
	 * 表示大于，>
	 */
	FILTER_GREATETHAN,
	/**
	 * 表示大于等于，>=
	 */
	FILTER_GREATEEQUAL,
	/**
	 * 表示小于，<
	 */
	FILTER_LESSTHAN,
	/**
	 * 表示小于等于，<=
	 */
	FILTER_LESSEQUAL,
	/**
	 * 表示不等于，<>
	 */
	FILTER_NOTEQUAL,
	/**
	 * 表示属于，"in"查询，in
	 */
	FILTER_IN,
	/**
	 * 表示不属于，"not in"查询，not in
	 */
	FILTER_NOTIN,
	/**
	 * 表示为空值(null)，is null
	 */
	FILTER_ISNULL,
	/**
	 * 表示不为空值(null)，is not null
	 */
	FILTER_ISNOTNULL,
	/**
	 * 表示增加设定值后大于条件判断，比如count + 10 > 0
	 */
	FILTER_UPGREATETHAN,
	/**
	 * 表示增加设定值后大于等于条件判断，比如count + 10 >= 0
	 */
	FILTER_UPGREATEEQUAL,
	/**
	 * 表示减少设定值后小于条件判断，比如count - 10 > 0
	 */
	FILTER_REDUCEGREATETHAN,
	/**
	 * 表示减少设定值后小于等于条件判断，比如count - 10 >= 0
	 */
	FILTER_REDUCEGREATEEQUAL,
	/**
	 * 表示左模糊查询，like
	 */
	FILTER_LEFT_LIKE,
	/**
	 * 表示右模糊查询，like
	 */
	FILTER_RIGHT_LIKE;
}
