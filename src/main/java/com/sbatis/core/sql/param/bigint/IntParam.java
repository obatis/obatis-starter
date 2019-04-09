package com.sbatis.core.sql.param.bigint;


import com.sbatis.core.constant.type.FilterEnum;

public class IntParam extends AbstractColumnParam {

	@Override
	public Object[] addFilter(String filterName, FilterEnum filterType, Object value, String joinType) {
		Object[] obj = {filterName, filterType, value, joinType};
		return obj;
	}

	
}
