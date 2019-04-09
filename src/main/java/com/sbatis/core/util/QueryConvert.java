package com.sbatis.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;

import com.sbatis.convert.date.DateCommonConvert;
import com.sbatis.core.annotation.param.QueryFilter;
import com.sbatis.core.annotation.param.UpdateField;
import com.sbatis.core.constant.type.DateFilterEnum;
import com.sbatis.core.constant.type.FilterEnum;
import com.sbatis.core.constant.type.SqlHandleEnum;
import com.sbatis.core.exception.HandleException;
import com.sbatis.core.sql.QueryProvider;
import com.sbatis.validate.ValidateTool;

public class QueryConvert {

	private QueryConvert() {
	}

	public static final void getFilters(Object obj, QueryProvider param) {
		getFilters(obj, obj.getClass(), param);
	}
	
	private static final void getFilters(Object obj, Class<?> cls, QueryProvider param) {
		Field[] fields = cls.getDeclaredFields();
		
		for (Field field : fields) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if (isStatic) {
				continue;
			}
			
			QueryFilter filter = field.getAnnotation(QueryFilter.class);
			if (filter == null) {
				continue;
			}
			
			String fieldName = !ValidateTool.isEmpty(filter.name()) ? filter.name() : field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!filter.isnull() && ValidateTool.isEmpty(value)) {
				continue;
			}
			
			DateFilterEnum dateType = filter.datetype();
			if(value instanceof Date) {
				if(DateFilterEnum.BEGIN.equals(dateType)) {
					value = DateCommonConvert.formatBeginDateTime((Date) value);
				} else if (DateFilterEnum.END.equals(dateType)) {
					value = DateCommonConvert.formatEndDateTime((Date) value);
				}
			}
			
			
			FilterEnum type = filter.type();
			switch (type) {
			case FILTER_EQUAL:
				param.addFilterEquals(fieldName, value);
				break;
			case FILTER_GREATETHAN:
				param.addFilterGreateThan(fieldName, value);
				break;
			case FILTER_GREATEEQUAL:
				param.addFilterGreateEqual(fieldName, value);
				break;
			case FILTER_LESSTHAN:
				param.addFilterLeftLike(fieldName, value);
				break;
			case FILTER_LESSEQUAL:
				param.addFilterLessEqual(fieldName, value);
				break;
			case FILTER_NOTEQUAL:
				param.addFilterNotEqual(fieldName, value);
				break;
			case FILTER_IN:
				param.addFilterIn(fieldName, value);
				break;
			case FILTER_NOTIN:
				param.addFilterNotIn(fieldName, value);
				break;
			case FILTER_ISNULL:
				param.addFilterIsNull(fieldName);
				break;
			case FILTER_ISNOTNULL:
				param.addFilterIsNotNull(fieldName);
				break;
			case FILTER_UPGREATETHAN:
				param.addFilterUpGreateThanZero(fieldName, value);
				break;
			case FILTER_REDUCEGREATETHAN:
				param.addFilterReduceGreateThanZero(fieldName, value);
				break;
			case FILTER_REDUCEGREATEEQUAL:
				param.addFilterReduceGreateEqualZero(fieldName, value);
				break;
			case FILTER_LEFT_LIKE:
				param.addFilterLeftLike(fieldName, value);
				break;
			case FILTER_RIGHT_LIKE:
				param.addFilterRightLike(fieldName, value);
				break;
			default:
				throw new HandleException("Error: filter field annotation is error !!!");
			}
			
		}
		
		Class<?> supCls = cls.getSuperclass();
		if(supCls != null) {
			getFilters(obj, supCls, param);
		}
	}
	
	public static final void getUpdateField(Object obj, QueryProvider param) {
		getUpdateField(obj, obj.getClass(), param);
	}
	
	private static void getUpdateField(Object obj, Class<?> cls, QueryProvider param) {
		Field[] fields = cls.getDeclaredFields();
		
		for (Field field : fields) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if (isStatic) {
				continue;
			}
			
			UpdateField uField = field.getAnnotation(UpdateField.class);
			if (uField == null) {
				continue;
			}
			
			String fieldName = !ValidateTool.isEmpty(uField.name()) ? uField.name() : field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!uField.isnull() && ValidateTool.isEmpty(value)) {
				continue;
			}
			
			SqlHandleEnum type = uField.type();
			switch (type) {
			case HANDLE_DEFAULT:
				// 表示常规类型
				param.addField(fieldName, value);
				break;
			case HANDLE_UP:
				// 表示累加
				param.addFieldUp(fieldName, value);
				break;
			case HANDLE_REDUCE:
				// 表示累加
				param.addFieldReduce(fieldName, value);
				break;
			default:
				throw new HandleException("Error: update field annotation is error !!!");
			}
		}
		
		Class<?> supClas = cls.getSuperclass();
		if(supClas != null) {
			getUpdateField(obj, supClas, param);
		}
	}
	
}
