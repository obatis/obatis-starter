package com.sbatis.core.mapper;

import java.util.HashMap;
import java.util.Map;

public class MapperCacheInfo {

	public static final Map<String, BaseBeanMapper<?>> BEAN_MAPPER = new HashMap<>();
	public static final Map<String, BaseResultMapper<?>> RESULT_MAPPER = new HashMap<>();
}