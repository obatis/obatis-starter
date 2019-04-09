package com.sbatis.core.sql;

import com.sbatis.convert.date.DateCommonConvert;
import com.sbatis.core.BaseCommonField;
import com.sbatis.core.annotation.NotColumn;
import com.sbatis.core.constant.CoreCommonStants;
import com.sbatis.core.exception.HandleException;
import com.sbatis.core.generator.NumberGenerator;
import com.sbatis.core.util.CacheInfoConstant;
import com.sbatis.validate.ValidateTool;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractInsertMethod {

	protected String getInsertSql(Object obj, Class<?> cls, String tableName) throws HandleException {

        SQL sql = new SQL();
        sql.INSERT_INTO(tableName);
        Map<String, String> res = this.getInsertFields(obj, cls, tableName);
        if (res == null) {
            throw new HandleException("insert error：object is empty！！！");
        }

        sql.INTO_COLUMNS(res.get(CoreCommonStants.BEAN_FIELD));
        sql.INTO_VALUES(res.get(CoreCommonStants.BEAN_VALUE));
        return sql.toString();
    }
	
	private Map<String, String> getInsertFields(Object obj, Class<?> cls, String tableName) throws HandleException {
		List<String> fieldArr = new ArrayList<>();
		List<String> valueArr = new ArrayList<>();

		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		getInsertFieldVaule(cls, columnMap, obj, fieldArr, valueArr);

		if (fieldArr.size() > 0) {
			Map<String, String> res = new HashMap<String, String>();
			res.put(CoreCommonStants.BEAN_FIELD, String.join(",", fieldArr));
			res.put(CoreCommonStants.BEAN_VALUE, String.join(",", valueArr));
			return res;
		} else {
			return null;
		}
	}
	
	private void getInsertFieldVaule(Class<?> cls, Map<String, String> columnMap, Object obj, List<String> fieldArr, List<String> valueArr)
			throws HandleException {

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
				boolean addFlag = false;
				if (ValidateTool.isEmpty(value)) {
					if (BaseCommonField.FIELD_ID.equals(columnName)) {
						field.set(obj, NumberGenerator.getNumber());
						addFlag = true;
					} else if (BaseCommonField.FIELD_CREATE_TIME.equals(columnName)) {
						field.set(obj, DateCommonConvert.getCurDate());
						addFlag = true;
					}
				} else {
					// 说明值不为空
					addFlag = true;
				}

				if (addFlag) {
					fieldArr.add(columnName);
					valueArr.add("#{param." + fieldName + "}");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new HandleException("Error: get fields fail!!!");
			}
		}

		Class<?> supCls = cls.getSuperclass();
		if (supCls != null) {
			getInsertFieldVaule(supCls, columnMap, obj, fieldArr, valueArr);
		}
	}
	
	protected abstract String getInsertBatchSql(List<?> list, Class<?> cls, String tableName);
}
