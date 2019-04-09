package com.sbatis.core.convert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pudahui.core.annotation.Table;
import com.sbatis.core.annotation.Column;
import com.sbatis.core.annotation.NotColumn;
import com.sbatis.core.constant.CacheInfoConstant;
import com.sbatis.core.constant.SqlConstant;
import com.sbatis.core.exception.HandleException;
import com.sbatis.validate.ValidateTool;

public class BeanConvert {

	private BeanConvert() {
	}
	
	public static final void initEntityCache(Class<?> cls) {
		Map<String, String> columnMap = new HashMap<>();
		Map<String, String> fieldMap = new HashMap<>();
		
		Table table = cls.getAnnotation(Table.class);
		String canonicalName = cls.getCanonicalName();
		if(ValidateTool.isEmpty(table)) {
			throw new HandleException("error: " + canonicalName + " tableName must be anotation!!!");
		}
		String name = table.name();
		if(ValidateTool.isEmpty(name)) {
			throw new HandleException("error: " + canonicalName + " tableName is empty!!!");
		}
		
		if(CacheInfoConstant.FIELD_CACHE.containsKey(name)) {
			throw new HandleException("error: " + canonicalName + " tableName(" + name + ") is exist!!!");
		}
		CacheInfoConstant.TABLE_CACHE.put(canonicalName, name);
		addColumnCache(cls, name, columnMap, fieldMap, 0);
	}

	private static final void addColumnCache(Class<?> cls, String tableName, Map<String, String> columnMap, Map<String, String> fieldMap, int index) {
		Field[] fields = cls.getDeclaredFields();

		for (Field field : fields) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if (isStatic) {
				continue;
			}
			NotColumn ts = field.getAnnotation(NotColumn.class);
			if (ts != null) {
				continue;
			}
			String fieldName = field.getName();
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				String name = column.name();
				if (ValidateTool.isEmpty(name)) {
					throw new HandleException("error: column annotaton name is not null");
				}
				columnMap.put(fieldName, name);
				/**
				 * 将有注解的属性，统一放进缓存方便取值
				 */
				if (!name.equals(fieldName)) {
					fieldMap.put(name, fieldName);
				}
			} else {
				columnMap.put(fieldName, fieldName);
			}
		}

		Class<?> supCls = cls.getSuperclass();
		if (supCls != null) {
			addColumnCache(supCls, tableName, columnMap, fieldMap, index + 1);
		}

		if (index == SqlConstant.DEFAULT_INIT) {
			int len = columnMap.size();
			if (len > 0) {
				CacheInfoConstant.FIELD_CACHE.put(tableName, fieldMap);
				CacheInfoConstant.COLUMN_CACHE.put(tableName, columnMap);
			}
		}
	}

	public static List<String[]> getResultFields(Class<?> cls) {
		String clsName = cls.getCanonicalName();
		if(CacheInfoConstant.RESULT_CACHE.containsKey(clsName)) {
			return CacheInfoConstant.RESULT_CACHE.get(clsName);
		} else {
			return getResultFields(cls, clsName);
		}
    }
	
	private static synchronized List<String[]> getResultFields(Class<?> cls, String clsName) {
		if(CacheInfoConstant.RESULT_CACHE.containsKey(clsName)) {
			return CacheInfoConstant.RESULT_CACHE.get(clsName);
		}
		
		List<String[]> resultList = new ArrayList<>();
		getResultFields(cls, resultList);
		CacheInfoConstant.RESULT_CACHE.put(clsName, resultList);
		return resultList;
	}
	
	private static void getResultFields(Class<?> cls, List<String[]> resultList) {
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if (isStatic) {
				continue;
			}
			NotColumn ts = field.getAnnotation(NotColumn.class);
			if (ts != null) {
				continue;
			}
			String fieldName = field.getName();
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				String name = column.name();
				if (ValidateTool.isEmpty(name)) {
					throw new HandleException("error: column annotaton name is not null");
				}
				String[] result = {name, fieldName};
				resultList.add(result);
			} else {
				String[] result = {fieldName, fieldName};
				resultList.add(result);
			}
		}

		/**
		 * 进行递归循环调用
		 */
		Class<?> supCls = cls.getSuperclass();
		if (supCls != null) {
			getResultFields(supCls, resultList);
		}
	}
}
