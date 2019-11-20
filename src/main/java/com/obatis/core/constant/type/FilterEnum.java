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
	 * 等于，= ，针对时间格式化使用
	 */
	EQUAL_DATE_FORMAT,
	/**
	 * 等于，= 针对字段
	 */
	EQUAL_FIELD,
	/**
	 * 大于，>
	 */
	GREATER_THAN,
	/**
	 * 大于，>， 针对时间格式化使用
	 */
	GREATER_THAN_DATE_FORMAT,
    /**
     * 大于，>
     */
    GREATER_THAN_FIELD,
	/**
	 * 大于等于，>=
	 */
	GREATER_EQUAL,
	/**
	 * 大于等于，>=,针对时间格式化使用
	 */
	GREATER_EQUAL_DATE_FORMAT,
    /**
     * 大于等于，>= 针对字段
     */
    GREATER_EQUAL_FIELD,
	/**
	 * 小于，<
	 */
	LESS_THAN,
	/**
	 * 小于，<,针对时间格式化使用
	 */
	LESS_THAN_DATE_FORMAT,
    /**
     * 小于，< 针对字段
     */
    LESS_THAN_FIELD,
	/**
	 * 小于等于，<=
	 */
	LESS_EQUAL,
	/**
	 * 小于等于，<= 针对时间格式化使用
	 */
	LESS_EQUAL_DATE_FORMAT,
    /**
     * 小于等于，<= 针对字段
     */
    LESS_EQUAL_FIELD,
	/**
	 * 不等于，<>
	 */
	NOT_EQUAL,
	/**
	 * 不等于，<>, 针对时间格式化使用
	 */
	NOT_EQUAL_DATE_FORMAT,
    /**
     * 不等于，<> 针对字段
     */
    NOT_EQUAL_FIELD,
	/**
	 * "in" 查询，in
	 */
	IN,
	/**
	 * 表示 in 嵌套子查询
	 */
	IN_PROVIDER,
	/**
	 * "not in" 嵌套子查询，not in
	 */
	NOT_IN_PROVIDER,
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
