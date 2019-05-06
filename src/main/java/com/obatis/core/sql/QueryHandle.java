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
			
			QueryFilter queryFilter = field.getAnnotation(QueryFilter.class);
			if (queryFilter == null) {
				continue;
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
				continue;
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
			case GREATE_THAN:
				queryProvider.greateThan(fieldName, value);
				break;
			case GREATE_EQUAL:
				queryProvider.greateEqual(fieldName, value);
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
			case UP_GREATE_THAN:
				queryProvider.upGreateThanZero(fieldName, value);
				break;
				case UP_GREATE_EQUAL:
				queryProvider.upGreateEqualZero(fieldName, value);
				break;
			case REDUCE_GREATE_THAN:
				queryProvider.reduceGreateThanZero(fieldName, value);
				break;
			case REDUCE_GREATE_EQUAL:
				queryProvider.reduceGreateEqualZero(fieldName, value);
				break;
			default:
				throw new HandleException("error: filter annotation invalid");
			}
			
		}
		
		Class<?> supCls = cls.getSuperclass();
		if(supCls != null) {
			getFilters(object, supCls, queryProvider);
		}
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
			
			UpdateField uField = field.getAnnotation(UpdateField.class);
			if (uField == null) {
				continue;
			}
			
			String fieldName = !ValidateTool.isEmpty(uField.name()) ? uField.name() : field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!uField.isnull() && ValidateTool.isEmpty(value)) {
				continue;
			}
			
			SqlHandleEnum type = uField.type();
			switch (type) {
			case HANDLE_DEFAULT:
				/**
				 * 常规类型操作
				 */
				queryProvider.add(fieldName, value);
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
