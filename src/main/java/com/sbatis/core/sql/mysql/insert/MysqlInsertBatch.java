package com.sbatis.core.sql.mysql.insert;

import com.sbatis.convert.date.DateCommonConvert;
import com.sbatis.core.BaseCommonEntity;
import com.sbatis.core.BaseCommonField;
import com.sbatis.core.annotation.NotColumn;
import com.sbatis.core.constant.CoreCommonStants;
import com.sbatis.core.exception.HandleException;
import com.sbatis.core.generator.NumberGenerator;
import com.sbatis.core.sql.AbstractInsertMethod;
import com.sbatis.core.util.CacheInfoConstant;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlInsertBatch extends AbstractInsertMethod {

	@Override
	protected String getInsertBatchSql(List<?> list, Class<?> cls, String tableName) {
		StringBuffer sql = new StringBuffer("insert into " + tableName + "(");
        Map<String, String> res = this.getInsertBatchFields(list, cls, tableName);
        if (res == null) {
            throw new HandleException("insert error：object is empty！！！");
        }

        sql.append(res.get(CoreCommonStants.BEAN_FIELD) + ")");
        return sql.toString() + " values " + res.get(CoreCommonStants.BEAN_VALUE);
	}

	protected Map<String, String> getInsertBatchFields(List<?> list, Class<?> cls, String tableName) {

		List<String> fieldArr = new ArrayList<String>();
		List<String> valueArr = new ArrayList<String>();

		if (list.isEmpty() || list.size() == 0) {
			throw new HandleException("Error: list is empty!!!");
		}

		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		for (int i = 0, j = list.size(); i < j; i++) {
			Object obj = list.get(i);
			if (!(obj instanceof BaseCommonEntity)) {
				throw new HandleException("error: the entity is not instanceof BaseCommonEntity!!!");
			}
			List<String> colValueArr = new ArrayList<String>();
			getInsertBatchValueColumnFields(obj, cls, columnMap, i, fieldArr, colValueArr);
			valueArr.add("(" + String.join(",", colValueArr) + ")");
		}

		if (fieldArr.size() > 0) {
			Map<String, String> res = new HashMap<String, String>();
			res.put(CoreCommonStants.BEAN_FIELD, String.join(",", fieldArr));
			res.put(CoreCommonStants.BEAN_VALUE, String.join(",", valueArr));
			return res;
		} else {
			return null;
		}
	}

	private void getInsertBatchValueColumnFields(Object obj, Class<?> cls, Map<String, String> columnMap, int index, List<String> fieldArr,
			List<String> colValueArr) {

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
			if (!columnMap.containsKey(fieldName)) {
				continue;
			}
			String columnName = columnMap.get(fieldName);

			try {
				field.setAccessible(true);
				Object value = field.get(obj);
				if (value == null || ("").equals(value.toString())) {
					if (BaseCommonField.FIELD_ID.equals(columnName)) {
						field.set(obj, NumberGenerator.getNumber());
					} else if (BaseCommonField.FIELD_CREATE_TIME.equals(columnName)) {
						field.set(obj, DateCommonConvert.getCurDate());
					}
				}

				if (index == 0) {
					fieldArr.add(columnName);
				}
				colValueArr.add("#{param[" + index + "]." + fieldName + "}");
			} catch (Exception e) {
				e.printStackTrace();
				throw new HandleException("Error: get fields fail!!!");
			}
		}

		Class<?> supCls = cls.getSuperclass();
		if (supCls != null) {
			getInsertBatchValueColumnFields(obj, supCls, columnMap, index, fieldArr, colValueArr);
		}
	}
}
