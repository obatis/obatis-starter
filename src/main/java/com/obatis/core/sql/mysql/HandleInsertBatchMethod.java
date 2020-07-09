package com.obatis.core.sql.mysql;

import com.obatis.convert.date.DateCommonConvert;
import com.obatis.core.CommonModel;
import com.obatis.core.CommonField;
import com.obatis.core.annotation.NotColumn;
import com.obatis.core.constant.SqlConstant;
import com.obatis.core.exception.HandleException;
import com.obatis.core.sql.AbstractInsertMethod;
import com.obatis.core.constant.CacheInfoConstant;
import com.obatis.generator.NumberGenerator;
import com.obatis.tools.ValidateTool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL 批量添加方法实现
 * @author HuangLongPu
 */
public class HandleInsertBatchMethod extends AbstractInsertMethod {

	@Override
	protected String handleBatchInsertSql(List<?> list, Class<?> cls, String tableName) {
		StringBuffer sql = new StringBuffer("insert into " + tableName + "(");
        Map<String, String> res = this.getBatchInsertFields(list, cls, tableName);
        if (res == null) {
            throw new HandleException("error：object is empty");
        }

        sql.append(res.get(SqlConstant.BEAN_FIELD) + ")");
        return sql.toString() + " values " + res.get(SqlConstant.BEAN_VALUE);
	}

	protected Map<String, String> getBatchInsertFields(List<?> list, Class<?> cls, String tableName) {

		List<String> fieldArr = new ArrayList<>();
		List<String> valueArr = new ArrayList<>();

		if (list.isEmpty() || list.size() == 0) {
			throw new HandleException("error: batch insert list is empty");
		}

		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		for (int i = 0, j = list.size(); i < j; i++) {
			Object obj = list.get(i);
			if (!(obj instanceof CommonModel)) {
				throw new HandleException("error: entity is not instanceof CommonModel");
			}
			List<String> colValueArr = new ArrayList<>();
			getBatchInsertValidColumnFields(obj, cls, columnMap, i, fieldArr, colValueArr);
			valueArr.add("(" + String.join(",", colValueArr) + ")");
		}

		if (fieldArr.size() > 0) {
			Map<String, String> res = new HashMap<>();
			res.put(SqlConstant.BEAN_FIELD, String.join(",", fieldArr));
			res.put(SqlConstant.BEAN_VALUE, String.join(",", valueArr));
			return res;
		} else {
			return null;
		}
	}

	private void getBatchInsertValidColumnFields(Object obj, Class<?> cls, Map<String, String> columnMap, int index, List<String> fieldArr,
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
					if (CommonField.FIELD_ID.equals(columnName)) {
						field.set(obj, NumberGenerator.getNumber());
					} else if (CommonField.FIELD_CREATE_TIME.equals(columnName)) {
						field.set(obj, DateCommonConvert.getDateTime());
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
			getBatchInsertValidColumnFields(obj, supCls, columnMap, index, fieldArr, colValueArr);
		}
	}
}
