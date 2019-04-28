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
			
			QueryFilter filter = field.getAnnotation(QueryFilter.class);
			if (filter == null) {
				continue;
			}
			
			String fieldName = !ValidateTool.isEmpty(filter.name()) ? filter.name() : field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!filter.isnull() && ValidateTool.isEmpty(value)) {
				continue;
			}
			
			DateHandleEnum dateType = filter.datetype();
			if(value instanceof Date) {
				if(DateHandleEnum.BEGIN_HANDLE.equals(dateType)) {
					value = DateCommonConvert.formatBeginDateTime((Date) value);
				} else if (DateHandleEnum.END_HANDLE.equals(dateType)) {
					value = DateCommonConvert.formatEndDateTime((Date) value);
				}
			}
			
			
			FilterEnum type = filter.type();
			switch (type) {
			case EQUAL:
				queryProvider.addFilterEquals(fieldName, value);
				break;
			case GREATE_THAN:
				queryProvider.addFilterGreateThan(fieldName, value);
				break;
			case GREATE_EQUAL:
				queryProvider.addFilterGreateEqual(fieldName, value);
				break;
			case LESS_THAN:
				queryProvider.addFilterLeftLike(fieldName, value);
				break;
			case LESS_EQUAL:
				queryProvider.addFilterLessEqual(fieldName, value);
				break;
			case NOT_EQUAL:
				queryProvider.addFilterNotEqual(fieldName, value);
				break;
			case IN:
				queryProvider.addFilterIn(fieldName, value);
				break;
			case NOT_IN:
				queryProvider.addFilterNotIn(fieldName, value);
				break;
			case IS_NULL:
				queryProvider.addFilterIsNull(fieldName);
				break;
			case IS_NOT_NULL:
				queryProvider.addFilterIsNotNull(fieldName);
				break;
			case UP_GREATE_THAN:
				queryProvider.addFilterUpGreateThanZero(fieldName, value);
				break;
			case REDUCE_GREATE_THAN:
				queryProvider.addFilterReduceGreateThanZero(fieldName, value);
				break;
			case REDUCE_GREATE_EQUAL:
				queryProvider.addFilterReduceGreateEqualZero(fieldName, value);
				break;
			case LEFT_LIKE:
				queryProvider.addFilterLeftLike(fieldName, value);
				break;
			case RIGHT_LIKE:
				queryProvider.addFilterRightLike(fieldName, value);
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
