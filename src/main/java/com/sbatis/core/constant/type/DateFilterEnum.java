package com.sbatis.core.constant.type;

/**
 * 时间操作类型
 * @author HuangLongPu
 */
public enum DateFilterEnum {

	/**
	 * 默认，表示对日期不作处理
	 */
	NOT_HANDLE, 
	/**
	 * 开始时间，自动处理为 yyyy-MM-dd 00:00:00 格式
	 */
	BEGIN, 
	/**
	 * 结束时间，自动处理为 yyyy-MM-dd 23:59:59 格式
	 */
	END;
}
