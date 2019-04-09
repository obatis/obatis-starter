package com.sbatis.core.sql.mysql;

import com.sbatis.convert.date.DateCommonConvert;
import com.sbatis.core.BaseCommonEntity;
import com.sbatis.core.BaseCommonField;
import com.sbatis.core.annotation.NotColumn;
import com.sbatis.core.constant.SqlConstant;
import com.sbatis.core.exception.HandleException;
import com.sbatis.core.generator.NumberGenerator;
import com.sbatis.core.sql.AbstractInsertMethod;
import com.sbatis.core.constant.CacheInfoConstant;
import com.sbatis.validate.ValidateTool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL 添加方法实现
 * @author HuangLongPu
 */
public class HandleInsertBatchMethod extends AbstractInsertMethod {

	protected String handleInsertBatchSql(List<?> list, Class<?> cls, String tableName) {
		StringBuffer sql = new StringBuffer("insert into " + tableName + "(");
        Map<String, String> res = this.getInsertBatchFields(list, cls, tableName);
        if (res == null) {
            throw new HandleException("error：object is empty");
        }

        sql.append(res.get(SqlConstant.BEAN_FIELD) + ")");
        return sql.toString() + " values " + res.get(SqlConstant.BEAN_VALUE);
	}

	protected Map<String, String> getInsertBatchFields(List<?> list, Class<?> cls, String tableName) {

		List<String> fieldArr = new ArrayList<>();
		List<String> valueArr = new ArrayList<>();

		if (list.isEmpty() || list.size() == 0) {
			throw new HandleException("error: list is empty!!!");
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
			res.put(SqlConstant.BEAN_FIELD, String.join(",", fieldArr));
			res.put(SqlConstant.BEAN_VALUE, String.join(",", valueArr));
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
				if (ValidateTool.isEmpty(value)) {
					if (BaseCommonField.FIELD_ID.equals(columnName)) {
						field.set(obj, NumberGenerator.getNumber());
					} else if (BaseCommonField.FIELD_CREATE_TIME.equals(columnName)) {
						field.set(obj, DateCommonConvert.getCurDate());
					}
				}

				if (index == 0) {
					fieldArr.add(columnName);
				}
				colValueArr.add("#{request[" + index + "]." + fieldName + "}");
			} catch (Exception e) {
				e.printStackTrace();
				throw new HandleException("error: load class fields fail");
			}
		}

		Class<?> supCls = cls.getSuperclass();
		if (supCls != null) {
			getInsertBatchValueColumnFields(obj, supCls, columnMap, index, fieldArr, colValueArr);
		}
	}
}
