package com.sbatis.core.sql.param.bigint;

import com.sbatis.core.constant.type.FilterEnum;

/**
 * 声明添加条件Filter基类方法
 * @author HuangLongPu
 */
public abstract class AbstractColumnParam {

	public abstract Object[] addFilter(String filterName, FilterEnum filterType, Object value, String joinType);
}
