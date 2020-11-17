package com.obatis.core.sql;

import com.obatis.convert.date.DateConvert;
import com.obatis.core.CommonField;
import com.obatis.core.annotation.NotColumn;
import com.obatis.core.constant.SqlConstant;
import com.obatis.core.exception.HandleException;
import com.obatis.core.constant.CacheInfoConstant;
import com.obatis.generator.NumberGenerator;
import com.obatis.tools.ValidateTool;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractInsertMethod {

	protected String handleInsertSql(Object object, Class<?> clsName, String tableName) throws HandleException {

        SQL sql = new SQL();
        sql.INSERT_INTO(tableName);
        Map<String, String> res = this.getInsertFields(object, clsName, tableName);
        if (res == null) {
            throw new HandleException("error：object is null");
        }

        sql.INTO_COLUMNS(res.get(SqlConstant.BEAN_FIELD));
        sql.INTO_VALUES(res.get(SqlConstant.BEAN_VALUE));
        return sql.toString();
    }
	
	private Map<String, String> getInsertFields(Object object, Class<?> clsName, String tableName) throws HandleException {
		List<String> fields = new ArrayList<>();
		List<String> values = new ArrayList<>();

		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		getInsertFieldVaule(clsName, columnMap, object, fields, values);

		if (fields.size() > 0) {
			Map<String, String> res = new HashMap<>();
			res.put(SqlConstant.BEAN_FIELD, String.join(",", fields));
			res.put(SqlConstant.BEAN_VALUE, String.join(",", values));
			return res;
		} else {
			return null;
		}
	}
	
	private void getInsertFieldVaule(Class<?> clsName, Map<String, String> columnMap, Object obj, List<String> fields, List<String> values)
			throws HandleException {

		Field[] fieldArr = clsName.getDeclaredFields();
		for (Field field : fieldArr) {

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
				boolean addFlag = false;
				if (ValidateTool.isEmpty(value)) {
					if (CommonField.FIELD_ID.equals(columnName)) {
						field.set(obj, NumberGenerator.getNumber());
						addFlag = true;
					} else if (CommonField.FIELD_CREATE_TIME.equals(columnName)) {
						field.set(obj, DateConvert.getDateTime());
						addFlag = true;
					}
				} else {
					// 说明值不为空
					addFlag = true;
				}

				if (addFlag) {
					fields.add(columnName);
					values.add("#{request." + fieldName + "}");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new HandleException("error: load class fields fail");
			}
		}

		Class<?> supCls = clsName.getSuperclass();
		if (supCls != null) {
			getInsertFieldVaule(supCls, columnMap, obj, fields, values);
		}
	}
	
	protected abstract String handleBatchInsertSql(List<?> list, Class<?> cls, String tableName);
}
