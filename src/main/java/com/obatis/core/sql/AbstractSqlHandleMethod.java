package com.obatis.core.sql;

import com.obatis.core.CommonField;
import com.obatis.core.constant.CacheInfoConstant;
import com.obatis.core.constant.SqlConstant;
import com.obatis.core.constant.type.FilterEnum;
import com.obatis.core.constant.type.SqlHandleEnum;
import com.obatis.core.exception.HandleException;
import com.obatis.validate.ValidateTool;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;

/**
 * sql方法抽象类
 * @author HuangLongPu
 */
public abstract class AbstractSqlHandleMethod {

	private final static String INDEX_DEFAULT = "0";
	private final static int DEFAULT_FIND = 0;
	private final static int NOT_FIND = 1;

//	private TableIndexCache cache;
//	private Map<String, String> tableAsNameMap = new HashMap<>();

	protected AbstractSqlHandleMethod() {

	}

	/**
	 * 获取别名入口
	 * @param cache
	 * @param tableAsNameSerialNumber
	 * @return
	 */
	private String getTableAsName(TableIndexCache cache, String tableAsNameSerialNumber) {
		return cache.getTableAsName(tableAsNameSerialNumber);
	}

	public String getUpdateSql(Map<String, Object> providers, String tableName) throws HandleException {
		QueryProvider queryProvider = (QueryProvider) providers.get(SqlConstant.PROVIDER_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		Map<String, Object> fieldValue = new HashMap<>();
		Map<String, Object> filterValue = new HashMap<>();
		providers.put(SqlConstant.PROVIDER_FIELD, fieldValue);
		providers.put(SqlConstant.PROVIDER_FILTER, filterValue);
		return this.getUpdateSql(queryProvider, tableName, INDEX_DEFAULT, columnMap, fieldMap, fieldValue, filterValue);
	}

	public String getUpdateBatchSql(Map<String, Object> providers, String tableName) throws HandleException {
		List<QueryProvider> list = (List<QueryProvider>) providers.get(SqlConstant.PROVIDER_OBJ);
		StringBuffer sql = new StringBuffer();
//		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
//		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		Map<String, Object> fieldValue = new HashMap<>();
		Map<String, Object> filterValue = new HashMap<>();

		for (int i = 0, j = list.size(); i < j; i++) {
			QueryProvider queryProvider = list.get(i);
			sql.append(this.getUpdateSql(queryProvider, tableName, i + "", CacheInfoConstant.COLUMN_CACHE.get(tableName), CacheInfoConstant.FIELD_CACHE.get(tableName), fieldValue, filterValue) + ";");
		}

		providers.put(SqlConstant.PROVIDER_FIELD, fieldValue);
		providers.put(SqlConstant.PROVIDER_FILTER, filterValue);
		return getBatchUpdateDbSql(sql.toString());
	}

	protected abstract String getBatchUpdateDbSql(String sql);

	private String getUpdateSql(QueryProvider queryProvider, String tableName, String index, Map<String, String> columnMap,
			Map<String, String> fieldMap, Map<String, Object> fieldValue, Map<String, Object> filterValue) {
		SQL sql = new SQL();
		sql.UPDATE(tableName);
		sql.SET(getUpdateField(queryProvider.getFields(), columnMap, fieldMap, index + "_u", fieldValue));
		List<Object[]> filters = queryProvider.getFilters();
		if (filters != null && !filters.isEmpty()) {
			sql.WHERE(getFilterSql(null, "", filters, queryProvider.getOrProviders(), filterValue, index + "_ut", columnMap,
					fieldMap, NOT_FIND));
		} else {
			throw new HandleException("error：filters is empty");
		}
		return sql.toString();
	}

	private String[] getUpdateField(List<Object[]> fields, Map<String, String> columnMap, Map<String, String> fieldMap,
			String index, Map<String, Object> fieldValue) throws HandleException {

		if (fields == null) {
			throw new HandleException("error：fields is null");
		}
		int fieldsLen = fields.size();
		if (fieldsLen == 0) {
			throw new HandleException("error：fields is null");
		}

		String[] setColumn = new String[fieldsLen];

		for (int i = 0; i < fieldsLen; i++) {
			Object[] obj = fields.get(i);
			String key = SqlConstant.PROVIDER_FIELD + "_v" + index + "_" + i;
			SqlHandleEnum fieldType = (SqlHandleEnum) obj[1];
			String fieldTypeValue = "";
			String fieldName = obj[0].toString();
			String columnName = columnMap.get(fieldName);
			if (ValidateTool.isEmpty(columnName) && fieldMap.containsKey(fieldName)) {
				columnName = fieldName;
			}
			if (ValidateTool.isEmpty(columnName)) {
				throw new HandleException("error：fieldName is invalid");
			}
			String name = columnName;
			if (SqlHandleEnum.HANDLE_UP.equals(fieldType)) {
				fieldTypeValue = name + " + ";
			} else if (SqlHandleEnum.HANDLE_REDUCE.equals(fieldType)) {
				fieldTypeValue = name + " - ";
			}
			setColumn[i] = name + "= " + fieldTypeValue + "#{request." + SqlConstant.PROVIDER_FIELD + "." + key + "}";
			fieldValue.put(key, obj[2]);
		}

		return setColumn;
	}

	public String getDeleteByIdSql(String tableName) throws HandleException {

		SQL sql = new SQL();
		sql.DELETE_FROM(tableName);
		sql.WHERE(CommonField.FIELD_ID + "=#{" + CommonField.FIELD_ID + "}");
		return sql.toString();
	}

	public String getDeleteSql(Map<String, Object> param, String tableName) throws HandleException {

		SQL sql = new SQL();
		sql.DELETE_FROM(tableName);
		QueryProvider queryProvider = (QueryProvider) param.get(SqlConstant.PROVIDER_OBJ);
		List<Object[]> filters = queryProvider.getFilters();
		if (filters != null && !filters.isEmpty()) {
			Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
			Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);
			Map<String, Object> value = new HashMap<>();
			sql.WHERE(getFilterSql(null, "", filters, queryProvider.getOrProviders(), value, INDEX_DEFAULT + "_dt", columnMap,
					fieldMap, NOT_FIND));
			// 放入值到map
			param.put(SqlConstant.PROVIDER_FILTER, value);
		} else {
			throw new HandleException("error：filters is empty");
		}
		return sql.toString();
	}

	/**
	 * 拼接查询条件，主要针对left join连表
	 * @param tableAliasName
	 * @param filters
	 * @param value
	 * @param index
	 * @param columnMap
	 * @param fieldMap
	 * @return
	 * @throws HandleException
	 */
	private String getFilterSql(TableIndexCache cache, String tableAliasName, List<Object[]> filters, Map<String, Object> value, String index, Map<String, String> columnMap, Map<String, String> fieldMap) throws HandleException {
		return getFilterSql(cache, tableAliasName, filters, null, value, index, columnMap, fieldMap, DEFAULT_FIND);
	}

	/**
	 * 根据传入的filter，获取条件filter的数组
	 * @author HuangLongPu
	 * @param filters
	 * @return
	 * @throws HandleException
	 */
	private String getFilterSql(TableIndexCache cache, String tableAliasName, List<Object[]> filters,
			List<QueryProvider> orProviders, Map<String, Object> value, String index, Map<String, String> columnMap, Map<String, String> fieldMap,
			int findType) throws HandleException {
		int filtersLen = 0;
		if (filters != null && !filters.isEmpty()) {
			filtersLen = filters.size();
		}

		String tableAliasNamePrefix = " ";
		if (DEFAULT_FIND == findType) {
			tableAliasNamePrefix = " " + tableAliasName + ".";
		}
		StringBuffer filterSql = new StringBuffer();

		for (int i = 0; i < filtersLen; i++) {
			Object[] obj = filters.get(i);
			String key = SqlConstant.PROVIDER_FILTER + "_v" + index + "_" + i;
			FilterEnum filterType = (FilterEnum) obj[1];
			String field = getField(obj[0].toString(), columnMap);

			String sql;
			String expression = "#{request." + SqlConstant.PROVIDER_FILTER + "." + key + "}";
			Object vue = obj[2];
			switch (filterType) {
			case LIKE:
//				sql = tableAliasNamePrefix + field + getFilterType(filterType);
				sql = getHandleField(cache, tableAliasNamePrefix, field, columnMap, fieldMap) + getFilterType(filterType);
				sql += getLikeSql(expression);
				value.put(key, vue);
				break;
			case LEFT_LIKE:
				sql = getHandleField(cache, tableAliasNamePrefix, field, columnMap, fieldMap) + getFilterType(filterType);
				sql += getLeftLikeSql(expression);
				value.put(key, vue);
				break;
			case RIGHT_LIKE:
				sql = getHandleField(cache, tableAliasNamePrefix, field, columnMap, fieldMap) + getFilterType(filterType);
				sql += getRightLikeSql(expression);
				value.put(key, vue);
				break;
			case IN:
			case NOT_IN:
				sql = getHandleField(cache, tableAliasNamePrefix, field, columnMap, fieldMap) + getFilterType(filterType);
				sql += "(" + modifyInFilter(vue, key, value) + ")";
				break;
			case UP_GREATER_THAN:
				sql = getAgFunction(cache, tableAliasNamePrefix, field, fieldMap, columnMap) + " + " + expression + ">0";
				value.put(key, vue);
				break;
			case UP_GREATER_EQUAL:
				sql = getAgFunction(cache, tableAliasNamePrefix, field, fieldMap, columnMap) + " + " + expression + ">=0";
				value.put(key, vue);
				break;
			case REDUCE_GREATER_THAN:
				sql = getAgFunction(cache, tableAliasNamePrefix, field, fieldMap, columnMap) + " - " + expression + ">0";
				value.put(key, vue);
				break;
			case REDUCE_GREATER_EQUAL:
				sql = getAgFunction(cache, tableAliasNamePrefix, field, fieldMap, columnMap) + " - " + expression + ">=0";
				value.put(key, vue);
				break;
			case IS_NULL:
			case IS_NOT_NULL:
				sql = getAgFunction(cache, tableAliasNamePrefix, field, fieldMap, columnMap) + getFilterType(filterType);
				break;
			case GREATER_THAN:
			case GREATER_EQUAL:
			case LESS_THAN:
			case LESS_EQUAL:
				sql = getAgFunction(cache, tableAliasNamePrefix, field, fieldMap, columnMap) + getFilterType(filterType);
				sql += expression;
				value.put(key, vue);
				break;
			case EQUAL_FIELD:
			case GREATER_THAN_FIELD:
			case GREATER_EQUAL_FIELD:
			case LESS_THAN_FIELD:
			case LESS_EQUAL_FIELD:
			case NOT_EQUAL_FIELD:
				sql = getAgFunction(cache, tableAliasNamePrefix, field, fieldMap, columnMap) + getFilterType(filterType);
				sql += getAgFunction(cache, tableAliasNamePrefix, getField(value.toString(), columnMap), fieldMap, columnMap);
				break;
			case EQUAL_DATE_FORMAT:
			case NOT_EQUAL_DATE_FORMAT:
			case GREATER_THAN_DATE_FORMAT:
			case GREATER_EQUAL_DATE_FORMAT:
			case LESS_THAN_DATE_FORMAT:
			case LESS_EQUAL_DATE_FORMAT:
//				"DATE_FORMAT(" + tableAliasNamePrefix + field + ",'" + obj[4] + "')";
				sql = "DATE_FORMAT(" + getHandleField(cache, tableAliasNamePrefix, field, columnMap, fieldMap) + ",'" + obj[4] + "')" + getFilterType(filterType);
				sql += expression;
				value.put(key, vue);
				break;
			default:
				sql = getHandleField(cache, tableAliasNamePrefix, field, columnMap, fieldMap) + getFilterType(filterType);
				sql += expression;
				value.put(key, vue);
				break;
			}

			if (i == 0) {
                /**
                 * 第一个条件直接拼接，不用区分是 and 还是 or
                 */
				filterSql.append(sql);
			} else {
				filterSql.append(obj[3] + sql);
			}
		}

		if (orProviders != null && !orProviders.isEmpty()) {
			for (int j = 0, l = orProviders.size(); j < l; j++) {
				QueryProvider queryProvider = orProviders.get(j);
				String orItemSql = getFilterSql(cache, tableAliasName, queryProvider.getFilters(), queryProvider.getOrProviders(), value, index + "_ot_" + j, columnMap, fieldMap, findType);
				if (!ValidateTool.isEmpty(orItemSql)) {
					if (ValidateTool.isEmpty(filterSql.toString())) {
						filterSql.append("(" + orItemSql + ")");
					} else {
						filterSql.append(" and (" + orItemSql + ")");
					}
				}
			}
		}

		return filterSql.toString();
	}

	private String getHandleField(TableIndexCache cache, String tableAliasName, String tempFieldName, Map<String, String> columnMap, Map<String, String> fieldMap) {
		if(tempFieldName.startsWith(CacheInfoConstant.TABLE_AS_START_PREFIX)) {
			String[] fieldArray = tempFieldName.split("[.]");
			String tableAsNameSerialNumber = fieldArray[0].substring(fieldArray[0].indexOf("_") + 1);
			String expFieldName = fieldArray[1];

			if(fieldMap.containsKey(expFieldName)) {
				return getTableAsName(cache, tableAsNameSerialNumber) + "." + expFieldName;
			} else if (columnMap.containsKey(tempFieldName)) {
				return getTableAsName(cache, tableAsNameSerialNumber) + "." + columnMap.get(expFieldName);
			} else {
				throw new HandleException("error: exp field invalid");
			}
		} else {
			return tableAliasName + tempFieldName;
		}
	}

	private String getField(String filterName, Map<String, String> columnMap) {
		String column = columnMap.get(filterName);
		String field;
		if (!ValidateTool.isEmpty(column)) {
			field = column;
		} else {
			field = filterName;
		}
		return field;
	}

	public String getReplaceSql(String sql, int index) {
		if (!sql.contains("?")) {
			return sql;
		}
		String expression = "#{request[" + index + "]}";
		sql = sql.replaceFirst("[?]", expression);
		index++;
		return getReplaceSql(sql, index);
	}

	/**
	 * in 查询参数处理
	 * @param obj
	 * @param key
	 * @param param
	 * @return
	 */
	protected String modifyInFilter(Object obj, String key, Map<String, Object> param) throws HandleException {

		if (obj == null) {
			throw new HandleException("error: select filter is empty");
		}

		// 由于in查询能够接收多种类型的数据，需要做处理
		if (obj.getClass().isArray()) {
			return modifyArrInFilter(obj, key, param);
		} else if (obj instanceof Collection<?>) {
			// 表示为集合
			Object vue = ((Collection<?>) obj).toArray();
			return modifyArrInFilter(vue, key, param);
		} else if (obj instanceof String) {
			// 说明是字符串
			String vue = obj.toString();
			if (vue.contains(",")) {
				return modifyArrInFilter(vue.split(","), key, param);
			} else {
				return modifyOneInFilter(obj, key, param);
			}
		} else {
			// 其他
			return modifyOneInFilter(obj, key, param);
		}

	}

	private String modifyArrInFilter(Object obj, String key, Map<String, Object> param) {
		// 判断是数组
		StringBuilder itemSql = new StringBuilder();
		int length = Array.getLength(obj);
		for (int i = 0; i < length; i++) {
			String itemKey = key + "_" + i;
			param.put(itemKey, Array.get(obj, i));
			itemSql.append("#{request." + SqlConstant.PROVIDER_FILTER + ".").append(itemKey).append("}");
			if (i != length - 1) {
				itemSql.append(",");
			}
		}
		return itemSql.toString();
	}

	private String modifyOneInFilter(Object obj, String key, Map<String, Object> param) {
		// 判断不是数组
		StringBuilder itemSql = new StringBuilder();
		String itemKey = key + "_" + 0;
		param.put(itemKey, obj);
		itemSql.append("#{request." + SqlConstant.PROVIDER_FILTER + ".").append(itemKey).append("}");
		return itemSql.toString();
	}

	public String getSelectByIdSql(String[] columns, BigInteger id, String tableName) {

		SQL sql = new SQL();
		sql.SELECT(columns);
		sql.FROM(tableName);
		sql.WHERE("id=#{id}");
		return sql.toString();
	}

	/**
	 * 根据map，拼接SQL
	 *
	 * @param param
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public String getSelectSql(Map<String, Object> param, String tableName) throws HandleException {

		QueryProvider queryProvider = (QueryProvider) param.get(SqlConstant.PROVIDER_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		TableIndexCache cache = new TableIndexCache();
		String tableAliasName = this.getTableAsName(cache, queryProvider.getTableAsNameSerialNumber());
		SQL sql = new SQL();
		List<String> column = new ArrayList<>();
		getSelectFieldColumns(queryProvider, cache, tableAliasName, columnMap, fieldMap, column);
		Map<String, Object> value = new HashMap<>();

		StringBuffer leftJoinFilterSql = new StringBuffer();
		List<String> groups = new ArrayList<>();
		List<String> orders = new ArrayList<>();
		sql.FROM(tableName + " " + tableAliasName + getLeftJoinTable(cache, tableAliasName, queryProvider.getLeftJoinProviders(), value, INDEX_DEFAULT + "_cl", leftJoinFilterSql, column, groups, orders));
		sql.SELECT(String.join(",", column));
		// 构建 group by 语句
		this.addGroupBy(groups, tableAliasName, columnMap, queryProvider);
		this.addOrder(orders, cache, tableAliasName, fieldMap, columnMap, queryProvider);

		StringBuffer filterSqlBuffer = new StringBuffer();
		List<Object[]> filters = queryProvider.getFilters();
		if (filters != null && !filters.isEmpty()) {
			String filterSql = getFilterSql(cache, tableAliasName, filters, queryProvider.getOrProviders(), value,
					INDEX_DEFAULT + "_tl", columnMap, fieldMap, DEFAULT_FIND);
			if(!ValidateTool.isEmpty(filterSql)) {
				filterSqlBuffer.append(filterSql);
			}
		}

		if(!ValidateTool.isEmpty(leftJoinFilterSql.toString())) {
			if(!ValidateTool.isEmpty(filterSqlBuffer.toString())) {
				filterSqlBuffer.append(" and " + leftJoinFilterSql.toString());
//				filterSql += " and " + leftJoinFilterSql.toString();
			} else {
				filterSqlBuffer.append(leftJoinFilterSql);
//				filterSql = leftJoinFilterSql.toString();
			}
		}
		if (!ValidateTool.isEmpty(filterSqlBuffer.toString())) {
			sql.WHERE(filterSqlBuffer.toString());
		}

		if(!value.isEmpty()) {
			// 放入值到map
			param.put(SqlConstant.PROVIDER_FILTER, value);
		}

		if (!groups.isEmpty()) {
			sql.GROUP_BY(groups.toArray(new String[groups.size()]));
		}

		if (!orders.isEmpty()) {
			sql.ORDER_BY(orders.toArray(new String[orders.size()]));
		}

//		if (PageEnum.IS_PAGE_TRUE == queryProvider.getIsPage()) {
//			return appendPageSql(sql.toString(), queryProvider.getPageNumber(), queryProvider.getPageSize());
//		}
		return sql.toString();
	}

	public String getValidateSql(Map<String, Object> param, String tableName) throws HandleException {
		QueryProvider queryProvider = (QueryProvider) param.get(SqlConstant.PROVIDER_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		TableIndexCache cache = new TableIndexCache();
//		String tableAliasName = cache.getTableAsName();
		String tableAliasName = this.getTableAsName(cache, queryProvider.getTableAsNameSerialNumber());
		SQL sql = new SQL();
		sql.SELECT("count(1)");
		Map<String, Object> value = new HashMap<>();

		StringBuffer leftJoinFilterSql = new StringBuffer();
		List<String> groups = new ArrayList<>();
		String table = tableName + " " + tableAliasName + getLeftJoinTable(cache, tableAliasName, queryProvider.getLeftJoinProviders(), value, INDEX_DEFAULT + "_cl", leftJoinFilterSql, null, groups, null);
		sql.FROM(table);

		// 处理 group by 语句
		this.addGroupBy(groups, tableAliasName, columnMap, queryProvider);

		StringBuffer filterSqlBuffer = new StringBuffer();
		List<Object[]> filters = queryProvider.getFilters();
		if ((filters != null && !filters.isEmpty())) {

			String filterSql = getFilterSql(cache, tableAliasName, filters, queryProvider.getOrProviders(), value,
					INDEX_DEFAULT + "_tl", columnMap, fieldMap, DEFAULT_FIND);
			if(!ValidateTool.isEmpty(filterSql)) {
				filterSqlBuffer.append(filterSql);
			}
		}

		if(!ValidateTool.isEmpty(leftJoinFilterSql.toString())) {
			if(!ValidateTool.isEmpty(filterSqlBuffer.toString())) {
				filterSqlBuffer.append(" and " + leftJoinFilterSql);
//				filterSql += " and " + leftJoinFilterSql.toString();
			} else {
				filterSqlBuffer.append(leftJoinFilterSql);
//				filterSql = leftJoinFilterSql.toString();
			}
		}
		if (!ValidateTool.isEmpty(filterSqlBuffer.toString())) {
			sql.WHERE(filterSqlBuffer.toString());
		}

		if(!value.isEmpty()) {
			// 放入值到map
			param.put(SqlConstant.PROVIDER_FILTER, value);
		}

		if (!groups.isEmpty()) {
			sql.GROUP_BY(groups.toArray(new String[groups.size()]));
		}
		return sql.toString();
	}

	private void addGroupBy(List<String> groups, String tableAsName, Map<String, String> columnMap, QueryProvider queryProvider) {
		List<Object[]> queryGroup = queryProvider.getGroups();
		if (queryGroup != null && !queryGroup.isEmpty()) {
			for (Object[] groupArray : queryGroup) {
				SqlHandleEnum handleEnum = (SqlHandleEnum) groupArray[1];
				String fieldName = getField(groupArray[0].toString(), columnMap);
				switch (handleEnum) {
					case HANDLE_DEFAULT:
						groups.add(tableAsName + "." + fieldName);
						break;
					case HANDLE_DATE_FORMAT:
						groups.add("DATE_FORMAT(" + tableAsName + "." + fieldName + ",'" + groupArray[2] + "')");
						break;
					default:
						break;
				}


			}
		}
	}

	/**
	 * 处理排序
	 * @param orders
	 * @param tableAliasName
	 * @param fieldMap
	 * @param columnMap
	 * @param queryProvider
	 */
	private void addOrder(List<String> orders, TableIndexCache cache, String tableAliasName, Map<String, String> fieldMap, Map<String, String> columnMap, QueryProvider queryProvider) {
		List<Object[]> queryOrder = queryProvider.getOrders();
		if (queryOrder != null && !queryOrder.isEmpty()) {
			for (Object[] orderArray : queryOrder) {
				String column = columnMap.get(orderArray[0]);
				String fieldName;
				if (!ValidateTool.isEmpty(column)) {
					fieldName = column;
				} else {
					fieldName = orderArray[0].toString();
				}

				SqlHandleEnum sqlHandleEnum = (SqlHandleEnum) orderArray[2];
				switch (sqlHandleEnum) {
					case HANDLE_DEFAULT:
						orders.add(tableAliasName + "." + fieldName + " " + orderArray[1]);
						break;
					case HANDLE_SUM:
						orders.add("sum(" + tableAliasName + "." + fieldName + ") " + orderArray[1]);
						break;
					case HANDLE_AVG:
						orders.add("avg(" + tableAliasName + "." + fieldName + ") " + orderArray[1]);
						break;
					case HANDLE_EXP:
						orders.add(getAgFunction(cache, tableAliasName, fieldName, fieldMap, columnMap) + " " + orderArray[1]);
						break;
				}
			}
		}
	}

	private String getLeftJoinTable(TableIndexCache cache, String tableAliasName, List<Object[]> leftJoinProviders, Map<String, Object> value, String index, StringBuffer leftJoinFilterSql, List<String> column, List<String> groups, List<String> orders) {

		if (leftJoinProviders == null || leftJoinProviders.isEmpty()) {
			return "";
		}

		StringBuffer sql = new StringBuffer();
		for (int l = 0, m = leftJoinProviders.size(); l < m; l++) {
			Object[] leftJoinArray = leftJoinProviders.get(l);
			QueryProvider childParam = (QueryProvider) leftJoinArray[2];
			String connectTableName = childParam.getJoinTableName();
			if (ValidateTool.isEmpty(connectTableName)) {
				throw new HandleException("error: connectTableName is null");
			}
//			String connectTableAliasName = cache.getTableAsName();
			String connectTableAliasName = this.getTableAsName(cache, childParam.getTableAsNameSerialNumber());
			Object fieldName = leftJoinArray[0];
			Object paramFieldName = leftJoinArray[1];

			sql.append(" left join " + connectTableName + " " + connectTableAliasName + " on ");
			if (fieldName instanceof String) {
				// 说明是单个
				sql.append(tableAliasName + "." + leftJoinArray[0] + "=" + connectTableAliasName + "." + paramFieldName);
			} else {
				String[] fieldArr = (String[]) fieldName;
				String[] paramFieldArr = (String[]) paramFieldName;
				// 说明是数组
				for (int i = 0, j = fieldArr.length; i < j; i++) {
					sql.append(tableAliasName + "." + fieldArr[i] + "=" + connectTableAliasName + "." + paramFieldArr[i]);
					if (i != j - 1) {
						sql.append(" and ");
					}
				}
			}

			Map<String, String> childColumnMap = CacheInfoConstant.COLUMN_CACHE.get(connectTableName);
			Map<String, String> childFieldMap = CacheInfoConstant.FIELD_CACHE.get(connectTableName);
			if(column != null) {
				getSelectFieldColumns(childParam, cache, connectTableAliasName, childColumnMap, childFieldMap, column);
			}

			this.addGroupBy(groups, connectTableAliasName, childColumnMap, childParam);
			if(orders != null) {
				this.addOrder(orders, cache, connectTableAliasName, childFieldMap, childColumnMap, childParam);
			}

			List<Object[]> onFilters = childParam.getOnFilters();
			if(onFilters != null && !onFilters.isEmpty()) {
				String onFilterSql = this.getFilterSql(cache, connectTableAliasName, onFilters, value, index + "_" + l, childColumnMap, childFieldMap);
				if(!ValidateTool.isEmpty(onFilterSql)) {
					sql.append(" and " + onFilterSql);
				}
			}

			if(childParam.getFilters() != null && !childParam.getFilters().isEmpty()) {
				String filterSql = this.getFilterSql(cache, connectTableAliasName, childParam.getFilters(), value, index + "_fl" + l, childColumnMap, childFieldMap);
				if(ValidateTool.isEmpty(leftJoinFilterSql.toString())) {
					leftJoinFilterSql.append(filterSql);
				} else {
					leftJoinFilterSql.append(" and " + filterSql);
				}
			}

			List<Object[]> paramLeftJoinProviders = childParam.getLeftJoinProviders();
			if (paramLeftJoinProviders != null && paramLeftJoinProviders.size() > 0) {
				sql.append(getLeftJoinTable(cache, connectTableAliasName, paramLeftJoinProviders, value, index + "_" + l, leftJoinFilterSql, column, groups, orders));
			}

		}

		return sql.toString();
	}

	/**
	 * 获取要查询的字段列数组
	 * @author HuangLongPu
	 * @param queryProvider
	 * @return
	 * @throws HandleException
	 */
	private void getSelectFieldColumns(QueryProvider queryProvider, TableIndexCache cache, String tableAliasName, Map<String, String> columnMap, Map<String, String> fieldMap, List<String> column)
			throws HandleException {
		List<Object[]> fields;
		boolean allFlag = true;
		if ((fields = queryProvider.getFields()) != null && fields.size() > 0) {
			allFlag = false;
		}

		tableAliasName += ".";

		Map<String, String> notFields = queryProvider.getNotFields();
		if (allFlag) {
            /**
             * 表示未查询全部字段，sql 语句例如：select * from demo ************
             * 为提升查询效率，不建议 sql 查询所有字段，打印一条日志进行提醒开发人员
             */
//            log.warn("*********** WARN : no suggest use sql >>>>>>>>>  select * from XXXXXX ********");
			for (Map.Entry<String, String> entry : columnMap.entrySet()) {
				String name = entry.getValue();
				String key = entry.getKey();
				if (notFields != null && (notFields.containsKey(name) || notFields.containsKey(key))) {
					continue;
				}
				String columnName = tableAliasName + name;
				if (name.equals(key)) {
					column.add(columnName);
				} else {
					column.add(columnName + " as " + key);
				}
			}

			// 获取left join
//			List<Object[]> leftJoinParams = queryProvider.getLeftJoinProviders();
//			if (leftJoinParams != null && !leftJoinParams.isEmpty()) {
//				getLeftJoinSelectColumn(cache, leftJoinParams, column);
//			}

			if (column.isEmpty()) {
				throw new HandleException("error：field is null");
			}
			return;
		}

		// 获取列
		getSelectColumn(cache, tableAliasName, column, fields, fieldMap, columnMap, notFields);

		// 获取left join
//		List<Object[]> leftJoinParams = queryProvider.getLeftJoinProviders();
//		if (leftJoinParams != null && !leftJoinParams.isEmpty()) {
//			getLeftJoinSelectColumn(cache, leftJoinParams, column);
//		}

		if (column.size() == 0) {
			throw new HandleException("error：field is null");
		}

	}

	/**
	 * 获取连接查询的字段
	 * @author HuangLongPu
	 * @param leftJoinProviders
	 * @param column
	 */
//	private void getLeftJoinSelectColumn(String leftJoinTableAliasName, List<Object[]> leftJoinProviders, List<String> column, Map<String, String> columnMap, Map<String, String> fieldMap) {
//
//		for (Object[] obj : leftJoinProviders) {
//			QueryProvider queryProvider = (QueryProvider) obj[2];
////			String tableAliasName = TableNameConvert.getTableAsName(queryProvider.getJoinTableName());
////			String tableAliasName = cache.getTableAsName();
////			Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(queryProvider.getJoinTableName());
////			Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(queryProvider.getJoinTableName());
//
//			List<Object[]> fields = null;
//			if ((fields = queryProvider.getFields()) != null && !fields.isEmpty()) {
//				getSelectColumn(leftJoinTableAliasName, column, queryProvider.getFields(), fieldMap, columnMap, queryProvider.getNotFields());
//			}
//
//			/**
//			 * 针对left join查询，不查询所有字段，字段必须指定
//			 * HuangLongPu 于 2019年07月02日修复
//			 */
////			else {
////				Map<String, String> notFields = queryProvider.getNotFields();
////				/**
////				 * 表示未查询全部字段，sql 语句例如：select * from demo ************
////				 * 为提升查询效率，不建议 sql 查询所有字段，打印一条日志进行提醒开发人员
////				 */
////				log.warn("*********** WARN : no suggest use sql >>>>>>>>>  select * from XXXXXX ********");
////				for (Map.Entry<String, String> entry : columnMap.entrySet()) {
////					String name = entry.getValue();
////					String key = entry.getKey();
////					if (notFields != null && (notFields.containsKey(name) || notFields.containsKey(key))) {
////						continue;
////					}
////					String columnName = tableAliasName + "." + name;
////					if (name.equals(key)) {
////						column.add(columnName);
////					} else {
////						column.add(columnName + " as " + key);
////					}
////				}
////			}
//
//
////			List<Object[]> childLeftJoinProviders = queryProvider.getLeftJoinProviders();
////			if (childLeftJoinProviders != null && !childLeftJoinProviders.isEmpty()) {
////				this.getLeftJoinSelectColumn(cache, childLeftJoinProviders, column);
////			}
//		}
//	}

	/**
	 * 获取需要查询的字段
	 * @author HuangLongPu
	 * @param tableAliasName
	 * @param column
	 * @param fields
	 * @param fieldMap
	 * @param columnMap
	 * @param notFields
	 */
	private void getSelectColumn(TableIndexCache cache, String tableAliasName, List<String> column, List<Object[]> fields, Map<String, String> fieldMap,
			Map<String, String> columnMap, Map<String, String> notFields) {
		// 别名加点
		if (!ValidateTool.isEmpty(tableAliasName) && !tableAliasName.contains(".")) {
			tableAliasName += ".";
		}
		for (Object[] obj : fields) {
			String fieldName = obj[0].toString();
			Object value = obj[2];

			String fieldTemp;
			if (columnMap.containsKey(fieldName)) {
				fieldTemp = columnMap.get(fieldName);
			} else {
				fieldTemp = fieldName;
			}
			String fieldAliaName = ValidateTool.isEmpty(value) ? "" : value.toString();
			if (ValidateTool.isEmpty(fieldAliaName) && fieldMap.containsKey(fieldTemp)) {
				fieldAliaName = fieldMap.get(fieldTemp);
			}

			if (notFields != null && (notFields.containsKey(fieldAliaName) || notFields.containsKey(fieldName) || notFields.containsKey(fieldTemp))) {
				continue;
			}
			SqlHandleEnum type = (SqlHandleEnum) obj[1];
			String columnName;
			String fieldAsTemp = ValidateTool.isEmpty(fieldAliaName) ? "" : " as " + fieldAliaName;
			switch (type) {
			case HANDLE_COUNT:
				// 说明是count查询
				if (ValidateTool.isEmpty(fieldName)) {
					column.add("count(1)" + fieldAsTemp);
				} else {
					column.add("count(distinct " + fieldTemp + ")" + fieldAsTemp);
				}
				break;
			case HANDLE_SUM:
				columnName = "sum(ifnull(" + getAgFunction(cache, tableAliasName, fieldTemp, fieldMap, columnMap) + ", 0))";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_MAX:
				columnName = "max(" + getAgFunction(cache, tableAliasName, fieldTemp, fieldMap, columnMap) + ")";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_MIN:
				columnName = "min(" + getAgFunction(cache, tableAliasName, fieldTemp, fieldMap, columnMap) + ")";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_AVG:
				columnName = "avg(ifnull(" + getAgFunction(cache, tableAliasName, fieldTemp, fieldMap, columnMap) + ", 0))";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_EXP:
				columnName = getAgFunction(cache, tableAliasName, fieldTemp, fieldMap, columnMap);
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_DATE_FORMAT:
				if (!fieldMap.containsKey(fieldTemp) && !columnMap.containsKey(fieldTemp)) {
					throw new HandleException("error: fieldName('" + fieldName + "')  is invalid");
				} else {
//					columnName = tableAliasName + fieldTemp;
//					column.add(columnName + fieldAsTemp);
					column.add("DATE_FORMAT(" + tableAliasName + fieldTemp + ",'" + obj[3] + "')" + fieldAsTemp);
				}
				break;
			default:
				if (!fieldMap.containsKey(fieldTemp) && !columnMap.containsKey(fieldTemp)) {
					throw new HandleException("error: fieldName('" + fieldName + "')  is invalid");
				} else {
					columnName = tableAliasName + fieldTemp;
					column.add(columnName + fieldAsTemp);
				}
				break;
			}
		}
	}

	/**
	 * 解析聚合函数，拼装SQL
	 * @author HuangLongPu
	 * @param tableAliasName
	 * @param fieldName
	 * @return
	 */
	private String getAgFunction(TableIndexCache cache, String tableAliasName, String fieldName, Map<String, String> fieldMap, Map<String, String> columnMap) {
		boolean replaceFlag = false;
		String fieldNameTemp = fieldName;
		if (fieldName.contains("+")) {
			fieldName = fieldName.replace("+", "}+{");
			fieldNameTemp = fieldNameTemp.replace("+", ",");
			replaceFlag = true;
		}
		if (fieldName.contains("-")) {
			fieldName = fieldName.replace("-", "}-{");
			fieldNameTemp = fieldNameTemp.replace("-", ",");
			if (!replaceFlag) {
				replaceFlag = true;
			}
		}
		if (fieldName.contains("*")) {
			fieldName = fieldName.replace("*", "}*{");
			fieldNameTemp = fieldNameTemp.replace("*", ",");
			if (!replaceFlag) {
				replaceFlag = true;
			}
		}
		if (fieldName.contains("/")) {
			fieldName = fieldName.replace("/", "}/{");
			fieldNameTemp = fieldNameTemp.replace("/", ",");
			if (!replaceFlag) {
				replaceFlag = true;
			}
		}
		if (fieldName.contains("(")) {
			fieldName = fieldName.replace("(", "}({");
			fieldNameTemp = fieldNameTemp.replace("(", ",");
			if (!replaceFlag) {
				replaceFlag = true;
			}
		}
		if (fieldName.contains(")")) {
			fieldName = fieldName.replace(")", "}){");
			fieldNameTemp = fieldNameTemp.replace(")", ",");
			if (!replaceFlag) {
				replaceFlag = true;
			}
		}

		if (replaceFlag) {
			fieldName = "{" + fieldName + "}";
			String[] fieldNameTempArr = fieldNameTemp.split(",");
			Map<String, String> fieldNameTempMap = new HashMap<>();
			Map<String, String> cacheFieldNameTempMap = new HashMap<>();
			for (String name : fieldNameTempArr) {
				if (ValidateTool.isEmpty(name)) {
					continue;
				}
				fieldNameTempMap.put(name.replace(" ", ""), name.replace(" ", ""));
				cacheFieldNameTempMap.put(name.replace(" ", ""), name);
			}

			for (Map.Entry<String, String> map : fieldNameTempMap.entrySet()) {
				String field = map.getValue();
//				String tempField = field.toLowerCase();

				if(field.startsWith(CacheInfoConstant.TABLE_AS_START_PREFIX)) {
					// 说明带有自定义别名
					// #tas_201919999.name
					String[] fieldArray = field.split("[.]");
					String tableAsNameSerialNumber = fieldArray[0].substring(fieldArray[0].indexOf("_") + 1);
					String expFieldName = fieldArray[1];
					if(columnMap.containsKey(field)) {
						fieldName = fieldName.replace("{" + field + "}", getTableAsName(cache, tableAsNameSerialNumber) + "." + columnMap.get(expFieldName));
					}  else {
						fieldName = fieldName.replace("{" + field + "}", getTableAsName(cache, tableAsNameSerialNumber) + "." + expFieldName);
					}
				} else {
					// {name}
					if(fieldMap.containsKey(field)) {
						fieldName = fieldName.replace("{" + field + "}", tableAliasName + field);
					} else if (columnMap.containsKey(field)) {
						fieldName = fieldName.replace("{" + field + "}", tableAliasName + columnMap.get(field));
					} else {
						fieldName = fieldName.replace("{" + field + "}", cacheFieldNameTempMap.get(map.getKey()));
					}
				}
			}

			return fieldName.replaceAll("[{}]", "");
		} else {
			String tempFieldName = fieldName.replace(" ", "");
			if(tempFieldName.startsWith(CacheInfoConstant.TABLE_AS_START_PREFIX)) {
				String[] fieldArray = tempFieldName.split("[.]");
				String tableAsNameSerialNumber = fieldArray[0].substring(fieldArray[0].indexOf("_") + 1);
				String expFieldName = fieldArray[1];

				if(fieldMap.containsKey(expFieldName)) {
					return getTableAsName(cache, tableAsNameSerialNumber) + "." + expFieldName;
				} else if (columnMap.containsKey(tempFieldName)) {
					return getTableAsName(cache, tableAsNameSerialNumber) + "." + columnMap.get(expFieldName);
				} else {
					throw new HandleException("error: exp field invalid");
				}
			}

			if(fieldMap.containsKey(tempFieldName)) {
				return tableAliasName + tempFieldName;
			} else if (columnMap.containsKey(tempFieldName)) {
				return tableAliasName + columnMap.get(tempFieldName);
			} else {
				return fieldName;
			}
		}
	}

	/**
	 * 传入查询的枚举类型值，判断条件类型
	 * @author HuangLongPu
	 * @param type
	 * @return
	 */
	protected String getFilterType(FilterEnum type) {

		String filterType = null;
		switch (type) {
		case LIKE:
			filterType = " like ";
			break;
		case LEFT_LIKE:
			filterType = " like ";
			break;
		case RIGHT_LIKE:
			filterType = " like ";
			break;
		case EQUAL:
		case EQUAL_DATE_FORMAT:
		case EQUAL_FIELD:
			filterType =  " = ";
			break;
		case GREATER_THAN:
		case GREATER_THAN_DATE_FORMAT:
		case GREATER_THAN_FIELD:
			filterType = " > ";
			break;
		case GREATER_EQUAL:
		case GREATER_EQUAL_DATE_FORMAT:
		case GREATER_EQUAL_FIELD:
			filterType = " >= ";
			break;
		case LESS_THAN:
		case LESS_THAN_DATE_FORMAT:
		case LESS_THAN_FIELD:
			filterType = " < ";
			break;
		case LESS_EQUAL:
		case LESS_EQUAL_DATE_FORMAT:
		case LESS_EQUAL_FIELD:
			filterType = " <= ";
			break;
		case NOT_EQUAL:
		case NOT_EQUAL_DATE_FORMAT:
		case NOT_EQUAL_FIELD:
			filterType = " <> ";
			break;
		case IN:
			filterType = " in ";
			break;
		case NOT_IN:
			filterType = " not in ";
			break;
		case IS_NULL:
			filterType = " is null ";
			break;
		case IS_NOT_NULL:
			filterType = " is not null ";
			break;
		default:
			break;
		}

		return filterType;
	}

	/**
	 * 获取分页查询 sql 语句
	 * @author HuangLongPu
	 * @param providers
	 * @param tableName
	 */
	public void getQueryPageSql(Map<String, Object> providers, String tableName) {

		SQL sql = new SQL();
		QueryProvider queryProvider = (QueryProvider) providers.get(SqlConstant.PROVIDER_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);
		TableIndexCache cache = new TableIndexCache();
		String tableAliasName = this.getTableAsName(cache, queryProvider.getTableAsNameSerialNumber());
		List<String> column = new ArrayList<>();
		getSelectFieldColumns(queryProvider, cache, tableAliasName, columnMap, fieldMap, column);
		Map<String, Object> value = new HashMap<>();

		StringBuffer leftJoinFilterSql = new StringBuffer();
		// 构造 group by 语句
		List<String> groups = new ArrayList<>();
		// 构造order by 语句
		List<String> orders = new ArrayList<>();
		String fromTable = tableName + " " + tableAliasName + getLeftJoinTable(cache, tableAliasName, queryProvider.getLeftJoinProviders(), value, INDEX_DEFAULT + "_cl", leftJoinFilterSql, column, groups, orders);
		sql.SELECT(String.join(",", column));
		sql.FROM(fromTable);
		// 分页的语句
		SQL totalSql = new SQL();
		totalSql.SELECT("count(1)");
		totalSql.FROM(fromTable);

		this.addGroupBy(groups, tableAliasName, columnMap, queryProvider);
		this.addOrder(orders, cache, tableAliasName, fieldMap, columnMap, queryProvider);

		StringBuffer filterSqlBuffer = new StringBuffer();
		List<Object[]> filters = queryProvider.getFilters();
		if ((filters != null && !filters.isEmpty())) {

			String filterSql = getFilterSql(cache, tableAliasName, filters, queryProvider.getOrProviders(), value,
					INDEX_DEFAULT + "_t", columnMap, fieldMap, DEFAULT_FIND);
			if(!ValidateTool.isEmpty(filterSql)) {
				filterSqlBuffer.append(filterSql);
			}
		}

		if(!ValidateTool.isEmpty(leftJoinFilterSql.toString())) {
			if(!ValidateTool.isEmpty(filterSqlBuffer.toString())) {
				filterSqlBuffer.append(" and " + leftJoinFilterSql);
//				filterSql += " and " + leftJoinFilterSql.toString();
			} else {
				filterSqlBuffer.append(leftJoinFilterSql);
//				filterSql = leftJoinFilterSql.toString();
			}
		}
//		if (!ValidateTool.isEmpty(filterSql)) {
//			sql.WHERE(filterSql);
//			totalSql.WHERE(filterSql);
//		}

		if(!ValidateTool.isEmpty(filterSqlBuffer.toString())) {
			sql.WHERE(filterSqlBuffer.toString());
			totalSql.WHERE(filterSqlBuffer.toString());
		}

		if(!value.isEmpty()) {
			// 放入值到map
			providers.put(SqlConstant.PROVIDER_FILTER, value);
		}

		if (!groups.isEmpty()) {
			sql.GROUP_BY(groups.toArray(new String[groups.size()]));
			totalSql.GROUP_BY(groups.toArray(new String[groups.size()]));
		}

		if (!orders.isEmpty()) {
			sql.ORDER_BY(orders.toArray(new String[orders.size()]));
		}

		if (groups != null && !groups.isEmpty()) {
			providers.put(SqlConstant.PROVIDER_COUNT_SQL, "select count(1) from (" + totalSql.toString() + ") s");
		} else {
			providers.put(SqlConstant.PROVIDER_COUNT_SQL, totalSql.toString());
		}

		providers.put(SqlConstant.PROVIDER_QUERY_SQL, sql.toString());
	}

	/**
	 * 获取like sql
	 * @author HuangLongPu
	 * @param expression  表达式
	 * @return String
	 */
	abstract protected String getLikeSql(String expression);

	/**
	 * 获取左like sql
	 * @author HuangLongPu
	 * @param expression 表达式
	 * @return String
	 */
	abstract protected String getLeftLikeSql(String expression);

	/**
	 * 获取右like sql
	 * @author HuangLongPu
	 * @param expression 表达式
	 * @return String
	 */
	abstract protected String getRightLikeSql(String expression);

	/**
	 * 增加分页
	 * @author HuangLongPu
	 * @param sql            原sql
	 * @param pageNumber     页码
	 * @param pageSize       当前页数量
	 * @return String
	 */
	abstract protected String appendPageSql(String sql, int pageNumber, int pageSize);

	/**
	 * 得到分页信息
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	protected int getPageLimit(int pageNumber, int pageSize) {
		return (pageNumber - 1) * pageSize;
	}

	protected int getLastPage(int pageNumber, int pageSize) {
		return pageNumber * pageSize;
	}

}
