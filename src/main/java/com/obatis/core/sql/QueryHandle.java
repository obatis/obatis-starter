package com.obatis.core.sql;

import com.obatis.convert.date.DateCommonConvert;
import com.obatis.core.annotation.request.QueryFilter;
import com.obatis.core.annotation.request.UpdateField;
import com.obatis.core.constant.type.DateHandleEnum;
import com.obatis.core.constant.type.FilterEnum;
import com.obatis.core.constant.type.SqlHandleEnum;
import com.obatis.core.exception.HandleException;
import com.obatis.validate.ValidateTool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;

public class QueryHandle {

	private QueryHandle() {
	}

	public static final void getFilters(Object object, QueryProvider queryProvider) {
		getFilters(object, object.getClass(), queryProvider);
	}
	
	private static final void getFilters(Object object, Class<?> cls, QueryProvider queryProvider) {
		Field[] fields = cls.getDeclaredFields();
		
		for (Field field : fields) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if (isStatic) {
				continue;
			}
			if(!setFilter(object, queryProvider, field)) {
				continue;
			}
		}
		
		Class<?> supCls = cls.getSuperclass();
		if(supCls != null) {
			getFilters(object, supCls, queryProvider);
		}
	}

	private static boolean setFilter(Object object, QueryProvider queryProvider, Field field) {
		QueryFilter queryFilter = field.getAnnotation(QueryFilter.class);
		if (queryFilter == null) {
			return false;
		}
		String fieldName = !ValidateTool.isEmpty(queryFilter.name()) ? queryFilter.name() : field.getName();
		field.setAccessible(true);
		Object value = null;
		try {
			value = field.get(object);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(!queryFilter.isnull() && ValidateTool.isEmpty(value)) {
			return false;
		}

		DateHandleEnum dateHandle = queryFilter.datetype();
		if(value instanceof Date) {
			if(DateHandleEnum.BEGIN_HANDLE.equals(dateHandle)) {
				value = DateCommonConvert.formatBeginDateTime((Date) value);
			} else if (DateHandleEnum.END_HANDLE.equals(dateHandle)) {
				value = DateCommonConvert.formatEndDateTime((Date) value);
			}
		}


		FilterEnum filterType = queryFilter.type();
		switch (filterType) {
			case LIKE:
				queryProvider.like(fieldName, value);
				break;
			case LEFT_LIKE:
				queryProvider.leftLike(fieldName, value);
				break;
			case RIGHT_LIKE:
				queryProvider.rightLike(fieldName, value);
				break;
			case EQUAL:
				queryProvider.equals(fieldName, value);
				break;
			case GREATER_THAN:
				queryProvider.greaterThan(fieldName, value);
				break;
			case GREATER_EQUAL:
				queryProvider.greaterEqual(fieldName, value);
				break;
			case LESS_THAN:
				queryProvider.lessThan(fieldName, value);
				break;
			case LESS_EQUAL:
				queryProvider.lessEqual(fieldName, value);
				break;
			case NOT_EQUAL:
				queryProvider.notEqual(fieldName, value);
				break;
			case IN:
				queryProvider.in(fieldName, value);
				break;
			case NOT_IN:
				queryProvider.notIn(fieldName, value);
				break;
			case IS_NULL:
				queryProvider.isNull(fieldName);
				break;
			case IS_NOT_NULL:
				queryProvider.isNotNull(fieldName);
				break;
			case UP_GREATER_THAN:
				queryProvider.upGreaterThanZero(fieldName, value);
				break;
			case UP_GREATER_EQUAL:
				queryProvider.upGreaterEqualZero(fieldName, value);
				break;
			case REDUCE_GREATER_THAN:
				queryProvider.reduceGreaterThanZero(fieldName, value);
				break;
			case REDUCE_GREATER_EQUAL:
				queryProvider.reduceGreaterEqualZero(fieldName, value);
				break;
			default:
				throw new HandleException("error: filter annotation invalid");
		}

		return true;
	}
	
	public static final void getUpdateField(Object object, QueryProvider queryProvider) {
		getUpdateField(object, object.getClass(), queryProvider);
	}
	
	private static void getUpdateField(Object object, Class<?> cls, QueryProvider queryProvider) {
		Field[] fields = cls.getDeclaredFields();
		
		for (Field field : fields) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if (isStatic) {
				continue;
			}

			/**
			 * 进行条件的连带处理
			 */
			setFilter(object, queryProvider, field);
			UpdateField updateField = field.getAnnotation(UpdateField.class);
			if (updateField == null) {
				continue;
			}

			
			String fieldName = !ValidateTool.isEmpty(updateField.name()) ? updateField.name() : field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!updateField.isnull() && ValidateTool.isEmpty(value)) {
				continue;
			}
			
			SqlHandleEnum type = updateField.type();
			switch (type) {
			case HANDLE_DEFAULT:
				/**
				 * 常规类型操作
				 */
				queryProvider.set(fieldName, value);
				break;
			case HANDLE_UP:
				/**
				 * 累加
				 */
				queryProvider.addUp(fieldName, value);
				break;
			case HANDLE_REDUCE:
				/**
				 * 累加
				 */
				queryProvider.addReduce(fieldName, value);
				break;
			default:
				throw new HandleException("error: update annotation invalid");
			}
		}
		
		Class<?> supClas = cls.getSuperclass();
		if(supClas != null) {
			getUpdateField(object, supClas, queryProvider);
		}
	}

}
