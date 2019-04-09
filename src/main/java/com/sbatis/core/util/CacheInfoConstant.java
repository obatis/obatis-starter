package com.sbatis.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheInfoConstant {

	public static final Map<String, String> TABLE_CACHE = new HashMap<>();
	public static final Map<String, Map<String, String>> COLUMN_CACHE = new HashMap<>();
	public static final Map<String, Map<String, String>> FIELD_CACHE = new HashMap<>();
	protected static final Map<String, List<String[]>> RESULT_CACHE = new HashMap<>();
}
