package com.sbatis.core.sql;

import com.sbatis.core.BaseCommonField;
import com.sbatis.core.constant.SqlConstant;
import com.sbatis.core.constant.type.FilterEnum;
import com.sbatis.core.constant.type.PageEnum;
import com.sbatis.core.constant.type.SqlHandleEnum;
import com.sbatis.core.exception.HandleException;
import com.sbatis.core.constant.CacheInfoConstant;
import com.sbatis.validate.ValidateTool;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;

/**
 * sql方法抽象类
 * 
 * @author HuangLongPu
 *
 */
public abstract class AbstractMethod {

	private final static String INDEX_DEFAULT = "0";
	private final static int DEFAULT_FIND = 0;
	private final static int NOT_FIND = 1;

	protected AbstractMethod() {

	}

	public String getUpdateSql(Map<String, Object> param, String tableName) throws HandleException {
		QueryProvider QueryProvider = (QueryProvider) param.get(SqlConstant.PARAM_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		Map<String, Object> fieldValue = new HashMap<String, Object>();
		Map<String, Object> filterValue = new HashMap<String, Object>();

		param.put(SqlConstant.PARAM_FIELD, fieldValue);
		param.put(SqlConstant.PARAM_FILTER, filterValue);

		return this.getUpdateSql(param, QueryProvider, tableName, INDEX_DEFAULT, columnMap, fieldMap, fieldValue, filterValue);
	}

	public String getUpdateBatchSql(Map<String, Object> param, String tableName) throws HandleException {
		List<QueryProvider> list = (List<QueryProvider>) param.get(SqlConstant.PARAM_OBJ);
		StringBuffer s = new StringBuffer();
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		Map<String, Object> fieldValue = new HashMap<String, Object>();
		Map<String, Object> filterValue = new HashMap<String, Object>();

		for (int i = 0, j = list.size(); i < j; i++) {
			QueryProvider QueryProvider = list.get(i);
			s.append(this.getUpdateSql(param, QueryProvider, tableName, i + "", columnMap, fieldMap, fieldValue, filterValue) + ";");
		}

		param.put(SqlConstant.PARAM_FIELD, fieldValue);
		param.put(SqlConstant.PARAM_FILTER, filterValue);
		return getUpdateBatchDbSql(s.toString());
	}

	protected abstract String getUpdateBatchDbSql(String sql);

	private String getUpdateSql(Map<String, Object> param, QueryProvider QueryProvider, String tableName, String index, Map<String, String> columnMap,
			Map<String, String> fieldMap, Map<String, Object> fieldValue, Map<String, Object> filterValue) {
		SQL sql = new SQL();
		sql.UPDATE(tableName);
		sql.SET(getUpdateField(param, QueryProvider.getFields(), columnMap, fieldMap, index, fieldValue));
		List<Object[]> filters = QueryProvider.getFilters();
		if (filters != null && !filters.isEmpty()) {
			sql.WHERE(getFilterSql(QueryProvider.getLeftJoinParams(), null, null, "", filters, QueryProvider.getOrParams(), filterValue, index, columnMap,
					fieldMap, NOT_FIND));
			// // 放入值到map
			// request.put(SqlConstant.PARAM_FILTER, value);
		} else {
			throw new HandleException("update error：filters is empty！！！");
		}
		return sql.toString();
	}

	private String[] getUpdateField(Map<String, Object> param, List<Object[]> fields, Map<String, String> columnMap, Map<String, String> fieldMap,
			String index, Map<String, Object> fieldValue) throws HandleException {

		if (fields == null) {
			throw new HandleException("column error：fields is null！！！");
		}
		int fieldsLen = fields.size();
		if (fieldsLen == 0) {
			throw new HandleException("column error：fields is null！！！");
		}

		String[] setColumn = new String[fieldsLen];
		// Map<String, Object> value = new HashMap<String, Object>();

		for (int i = 0; i < fieldsLen; i++) {
			Object[] obj = fields.get(i);
			String key = SqlConstant.PARAM_FIELD + "_v" + index + "_" + i;
			SqlHandleEnum fieldType = (SqlHandleEnum) obj[1];
			String fieldTypeValue = "";
			String fieldName = obj[0].toString();
			String columnName = columnMap.get(fieldName);
			if (ValidateTool.isEmpty(columnName) && fieldMap.containsKey(fieldName)) {
				columnName = fieldName;
			}
			if (ValidateTool.isEmpty(columnName)) {
				throw new HandleException("column error：fieldName is invalid！！！");
			}
			String name = columnName;
			if (SqlHandleEnum.HANDLE_UP.equals(fieldType)) {
				fieldTypeValue = name + " + ";
			} else if (SqlHandleEnum.HANDLE_REDUCE.equals(fieldType)) {
				fieldTypeValue = name + " - ";
			}
			setColumn[i] = name + "= " + fieldTypeValue + "#{request." + SqlConstant.PARAM_FIELD + "." + key + "}";
			fieldValue.put(key, obj[2]);
		}

		// request.put(SqlConstant.PARAM_FIELD, value);
		return setColumn;
	}

	public String getDeleteByIdSql(String tableName) throws HandleException {

		SQL sql = new SQL();
		sql.DELETE_FROM(tableName);
		sql.WHERE(BaseCommonField.FIELD_ID + "=#{" + BaseCommonField.FIELD_ID + "}");
		return sql.toString();
	}

	public String getDeleteSql(Map<String, Object> param, String tableName) throws HandleException {

		SQL sql = new SQL();
		sql.DELETE_FROM(tableName);
		QueryProvider QueryProvider = (QueryProvider) param.get(SqlConstant.PARAM_OBJ);
		List<Object[]> filters = QueryProvider.getFilters();
		if (filters != null && !filters.isEmpty()) {
			Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
			Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);
			Map<String, Object> value = new HashMap<String, Object>();
			sql.WHERE(getFilterSql(QueryProvider.getLeftJoinParams(), null, null, "", filters, QueryProvider.getOrParams(), value, INDEX_DEFAULT, columnMap,
					fieldMap, NOT_FIND));
			// 放入值到map
			param.put(SqlConstant.PARAM_FILTER, value);
		} else {
			throw new HandleException("delete error：filters is empty！！！");
		}
		return sql.toString();
	}

	/**
	 * 根据传入的filter，获取条件filter的数组
	 *
	 * @param leftJoinParams
	 * @param filters
	 * @return
	 * @throws HandleException
	 */
	private String getFilterSql(List<Object[]> leftJoinParams, List<String> groups, List<String> orders, String tableAsName, List<Object[]> filters,
			List<QueryProvider> orParams, Map<String, Object> value, String index, Map<String, String> columnMap, Map<String, String> fieldMap,
			int findType) throws HandleException {
		int filtersLen = 0;
		if (filters != null && !filters.isEmpty()) {
			filtersLen = filters.size();
		}

		String tableAsNamePrefix = " ";
		if (DEFAULT_FIND == findType) {
			tableAsNamePrefix = " " + tableAsName + ".";
		}
		StringBuffer filterSql = new StringBuffer();

		for (int i = 0; i < filtersLen; i++) {
			Object[] obj = filters.get(i);
			String key = SqlConstant.PARAM_FILTER + "_v" + index + "_" + i;
			FilterEnum filterType = (FilterEnum) obj[1];
			String filterName = obj[0].toString();
			String column = columnMap.get(filterName);

			String field;
			if (!ValidateTool.isEmpty(column)) {
				field = column;
			} else {
				field = filterName;
			}

			String sql;
			String expression = "#{request." + SqlConstant.PARAM_FILTER + "." + key + "}";
			Object vue = obj[2];
			switch (filterType) {
			case FILTER_LIKE:
				sql = tableAsNamePrefix + field + getFilterType(filterType);
				sql += getLikeSql(expression);
				value.put(key, vue);
				break;
			case FILTER_LEFT_LIKE:
				sql = tableAsNamePrefix + field + getFilterType(filterType);
				sql += getLeftLikeSql(expression);
				value.put(key, vue);
				break;
			case FILTER_RIGHT_LIKE:
				sql = tableAsNamePrefix + field + getFilterType(filterType);
				sql += getRightLikeSql(expression);
				value.put(key, vue);
				break;
			case FILTER_IN:
			case FILTER_NOTIN:
				sql = tableAsNamePrefix + field + getFilterType(filterType);
				sql += "(" + modifyInFilter(vue, key, value) + ")";
				break;
			case FILTER_UPGREATETHAN:
				sql = getAgFunction(tableAsNamePrefix, field) + " + " + expression + ">0";
				value.put(key, vue);
				break;
			case FILTER_UPGREATEEQUAL:
				sql = getAgFunction(tableAsNamePrefix, field) + " + " + expression + ">=0";
				value.put(key, vue);
				break;
			case FILTER_REDUCEGREATETHAN:
				sql = getAgFunction(tableAsNamePrefix, field) + " - " + expression + ">0";
				value.put(key, vue);
				break;
			case FILTER_REDUCEGREATEEQUAL:
				sql = getAgFunction(tableAsNamePrefix, field) + " - " + expression + ">=0";
				value.put(key, vue);
				break;
			case FILTER_ISNULL:
			case FILTER_ISNOTNULL:
				sql = getAgFunction(tableAsNamePrefix, field) + getFilterType(filterType);
				break;
			case FILTER_GREATETHAN:
			case FILTER_GREATEEQUAL:
			case FILTER_LESSTHAN:
			case FILTER_LESSEQUAL:
				sql = getAgFunction(tableAsNamePrefix, field) + getFilterType(filterType);
				sql += expression;
				value.put(key, vue);
				break;
			default:
				sql = tableAsNamePrefix + field + getFilterType(filterType);
				sql += expression;
				value.put(key, vue);
				break;
			}

			if (i == 0) {
				// 说明是第一个条件，直接拼接，不管是什么条件
				filterSql.append(sql);
			} else {
				filterSql.append(obj[3] + sql);
			}
		}

		if (orParams != null && !orParams.isEmpty()) {
			for (int j = 0, l = orParams.size(); j < l; j++) {
				QueryProvider param = orParams.get(j);
				String orItemSql = getFilterSql(param.getLeftJoinParams(), groups, orders, tableAsName, param.getFilters(), param.getOrParams(), value, index
						+ "_" + j, columnMap, fieldMap, findType);
				if (!ValidateTool.isEmpty(orItemSql)) {
					if (ValidateTool.isEmpty(filterSql.toString())) {
						filterSql.append("(" + orItemSql + ")");
					} else {
						filterSql.append(" and (" + orItemSql + ")");
					}
				}
			}
		}

		if (leftJoinParams != null && !leftJoinParams.isEmpty()) {
			for (int j = 0, k = leftJoinParams.size(); j < k; j++) {
				Object[] obj = leftJoinParams.get(j);
				QueryProvider leftJoinParam = (QueryProvider) obj[2];
				String childTableAsName = TableNameConvert.getTableAsName(leftJoinParam.getJoinTableName());
				Map<String, String> childFieldMap = CacheInfoConstant.FIELD_CACHE.get(leftJoinParam.getJoinTableName());
				Map<String, String> childColumnMap = CacheInfoConstant.COLUMN_CACHE.get(leftJoinParam.getJoinTableName());
				this.getGroupBy(groups, childTableAsName, childColumnMap, leftJoinParam);
				this.getOrder(orders, childTableAsName, childColumnMap, leftJoinParam);
				String leftJoinFilterSql = getFilterSql(leftJoinParam.getLeftJoinParams(), groups, orders, childTableAsName, leftJoinParam.getFilters(),
						leftJoinParam.getOrParams(), value, index + "_l_" + j, childColumnMap, childFieldMap, findType);
				if (!ValidateTool.isEmpty(leftJoinFilterSql)) {
					if (ValidateTool.isEmpty(filterSql.toString())) {
						filterSql.append(leftJoinFilterSql);
					} else {
						filterSql.append(" and " + leftJoinFilterSql);
					}
				}
			}

		}

		return filterSql.toString();
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
	 *
	 * @param obj
	 * @param key
	 * @param param
	 * @return
	 */
	protected String modifyInFilter(Object obj, String key, Map<String, Object> param) throws HandleException {

		if (obj == null) {
			throw new HandleException("modify sql error: type in select filter is empty!");
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
			itemSql.append("#{request." + SqlConstant.PARAM_FILTER + ".").append(itemKey).append("}");
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
		itemSql.append("#{request." + SqlConstant.PARAM_FILTER + ".").append(itemKey).append("}");
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

		QueryProvider QueryProvider = (QueryProvider) param.get(SqlConstant.PARAM_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		String tableAsName = TableNameConvert.getTableAsName(tableName);
		SQL sql = new SQL();
		sql.SELECT(getSelectFieldColumns(QueryProvider, tableAsName, columnMap, fieldMap));
		sql.FROM(tableName + " " + tableAsName + getLeftJoinTable(tableAsName, QueryProvider.getLeftJoinParams()));

		// 分页的语句
		// SQL countSql = new SQL();
		// countSql.SELECT("count(1)");
		// countSql.FROM(tableName + " " + tableAsName +
		// getLeftJoinTable(tableAsName, QueryProvider.getLeftJoinParams()));

		// 构建 group by 语句
		List<String> groups = new ArrayList<String>();
		List<String> orders = new ArrayList<String>();
		this.getGroupBy(groups, tableAsName, columnMap, QueryProvider);
		this.getOrder(orders, tableAsName, columnMap, QueryProvider);

		List<Object[]> filters = QueryProvider.getFilters();
		if ((filters != null && !filters.isEmpty()) || (QueryProvider.getLeftJoinParams() != null && !QueryProvider.getLeftJoinParams().isEmpty())) {
			Map<String, Object> value = new HashMap<String, Object>();
			String filterSql = getFilterSql(QueryProvider.getLeftJoinParams(), groups, orders, tableAsName, filters, QueryProvider.getOrParams(), value,
					INDEX_DEFAULT, columnMap, fieldMap, DEFAULT_FIND);
			if (!ValidateTool.isEmpty(filterSql)) {
				// 放入值到map
				param.put(SqlConstant.PARAM_FILTER, value);
				sql.WHERE(filterSql);
				// countSql.WHERE(filterSql);
			}
		}

		if (!groups.isEmpty()) {
			sql.GROUP_BY(groups.toArray(new String[groups.size()]));
			// countSql.GROUP_BY(groups.toArray(new String[groups.size()]));
		}

		if (!orders.isEmpty()) {
			sql.ORDER_BY(orders.toArray(new String[orders.size()]));
		}

		if (PageEnum.IS_PAGE_TRUE == QueryProvider.getIsPage()) {
			return appendPageSql(sql.toString(), QueryProvider.getIndexPage(), QueryProvider.getPageSize(), false);
		}

		return sql.toString();
	}

	public String getValidateSql(Map<String, Object> param, String tableName) throws HandleException {

		QueryProvider QueryProvider = (QueryProvider) param.get(SqlConstant.PARAM_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);

		String tableAsName = TableNameConvert.getTableAsName(tableName);
		SQL sql = new SQL();
		sql.SELECT("count(1)");
		sql.FROM(tableName + " " + tableAsName + getLeftJoinTable(tableAsName, QueryProvider.getLeftJoinParams()));

		// 处理 group by 语句
		List<String> groups = new ArrayList<String>();
		this.getGroupBy(groups, tableAsName, columnMap, QueryProvider);

		List<Object[]> filters = QueryProvider.getFilters();
		if ((filters != null && !filters.isEmpty()) || (QueryProvider.getLeftJoinParams() != null && !QueryProvider.getLeftJoinParams().isEmpty())) {
			Map<String, Object> value = new HashMap<String, Object>();
			String filterSql = getFilterSql(QueryProvider.getLeftJoinParams(), groups, null, tableAsName, filters, QueryProvider.getOrParams(), value,
					INDEX_DEFAULT, columnMap, fieldMap, DEFAULT_FIND);
			if (!ValidateTool.isEmpty(filterSql)) {
				// 放入值到map
				param.put(SqlConstant.PARAM_FILTER, value);
				sql.WHERE(filterSql);
			}
		}
		if (!groups.isEmpty()) {
			sql.GROUP_BY(groups.toArray(new String[groups.size()]));
		}
		return sql.toString();
	}

	private void getGroupBy(List<String> groups, String tableAsName, Map<String, String> columnMap, QueryProvider QueryProvider) {
		List<String> queryGroup = QueryProvider.getGroups();
		if (queryGroup != null && !queryGroup.isEmpty()) {
			for (String field : queryGroup) {
				String column = columnMap.get(field);
				String fieldName;
				if (!ValidateTool.isEmpty(column)) {
					fieldName = column;
				} else {
					fieldName = field;
				}
				groups.add(tableAsName + "." + fieldName);
			}
		}
	}
	
	private void getOrder(List<String> orders, String tableAsName, Map<String, String> columnMap, QueryProvider QueryProvider) {
		List<String[]> queryOrder = QueryProvider.getOrders();
		if (queryOrder != null && !queryOrder.isEmpty()) {
			for (String[] field : queryOrder) {
				String column = columnMap.get(field[0]);
				String fieldName;
				if (!ValidateTool.isEmpty(column)) {
					fieldName = column;
				} else {
					fieldName = field[0];
				}
				orders.add(tableAsName + "." + fieldName + " " + field[1]);
			}
		}
	}

	private String getLeftJoinTable(String tableAsName, List<Object[]> leftJoinParams) {

		if (leftJoinParams == null || leftJoinParams.size() == 0) {
			return "";
		}

		StringBuffer sql = new StringBuffer();
		for (Object[] leftJoinArray : leftJoinParams) {

			QueryProvider childParam = (QueryProvider) leftJoinArray[2];
			String connectTableName = childParam.getJoinTableName();
			if (ValidateTool.isEmpty(connectTableName)) {
				throw new HandleException("set connectTableName Error:connectTableName can't null(empty)!!!");
			}
			String connectTableAsName = TableNameConvert.getTableAsName(connectTableName);
			Object fieldName = leftJoinArray[0];
			Object paramFieldName = leftJoinArray[1];

			sql.append(" left join " + connectTableName + " " + connectTableAsName + " on ");
			if (fieldName instanceof String) {
				// 说明是单个
				sql.append(tableAsName + "." + leftJoinArray[0] + "=" + connectTableAsName + "." + paramFieldName);
			} else {
				String[] fieldArr = (String[]) fieldName;
				String[] paramFieldArr = (String[]) paramFieldName;
				// 说明是数组
				for (int i = 0, j = fieldArr.length; i < j; i++) {
					sql.append(tableAsName + "." + fieldArr[i] + "=" + connectTableAsName + "." + paramFieldArr[i]);
					if (i != j - 1) {
						sql.append(" and ");
					}
				}
			}

			List<Object[]> childLeftJoinParams = childParam.getLeftJoinParams();
			if (childLeftJoinParams != null && childLeftJoinParams.size() > 0) {
				sql.append(getLeftJoinTable(connectTableAsName, childLeftJoinParams));
			}
		}

		return sql.toString();
	}

	/**
	 * 获取要查询的字段列数组
	 * 
	 * @param QueryProvider
	 * @return
	 * @throws HandleException
	 */
	private String getSelectFieldColumns(QueryProvider QueryProvider, String tableAsName, Map<String, String> columnMap, Map<String, String> fieldMap)
			throws HandleException {
		List<Object[]> fields = null;
		boolean allFlag = true;
		if ((fields = QueryProvider.getFields()) != null && fields.size() > 0) {
			allFlag = false;
		}

		tableAsName += ".";
		List<String> column = new ArrayList<String>();
		Map<String, String> notFields = QueryProvider.getNotFields();
		if (allFlag) {
			// 说明是 select * from SQL结构
			for (Map.Entry<String, String> entry : columnMap.entrySet()) {
				String name = entry.getValue();
				String key = entry.getKey();
				if (notFields != null && (notFields.containsKey(name) || notFields.containsKey(key))) {
					continue;
				}
				String columnName = tableAsName + name;
				if (name.equals(key)) {
					column.add(columnName);
				} else {
					column.add(columnName + " as " + key);
				}
			}

			// 获取left join
			List<Object[]> leftJoinParams = QueryProvider.getLeftJoinParams();
			if (leftJoinParams != null && !leftJoinParams.isEmpty()) {
				getLeftJoinSelectColumn(leftJoinParams, column);
			}

			if (column.size() == 0) {
				throw new HandleException("select field Error：field can't null ！！！");
			}
			return String.join(",", column);
		}

		// 获取列
		getSelectColumn(tableAsName, column, fields, fieldMap, columnMap, notFields);

		// 获取left join
		List<Object[]> leftJoinParams = QueryProvider.getLeftJoinParams();
		if (leftJoinParams != null && !leftJoinParams.isEmpty()) {
			getLeftJoinSelectColumn(leftJoinParams, column);
		}

		if (column.size() == 0) {
			throw new HandleException("select field Error：field can't null ！！！");
		}

		return String.join(",", column);
	}

	private void getLeftJoinSelectColumn(List<Object[]> leftJoinParams, List<String> column) {

		for (Object[] obj : leftJoinParams) {
			QueryProvider leftJoinParam = (QueryProvider) obj[2];
			String tableAsName = TableNameConvert.getTableAsName(leftJoinParam.getJoinTableName());
			Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(leftJoinParam.getJoinTableName());
			Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(leftJoinParam.getJoinTableName());
			
			List<Object[]> fields = null;
			if ((fields = leftJoinParam.getFields()) != null && fields.size() > 0) {
				getSelectColumn(tableAsName, column, leftJoinParam.getFields(), fieldMap, columnMap, leftJoinParam.getNotFields());
			} else {
				Map<String, String> notFields = leftJoinParam.getNotFields();
				// 说明是 select * from SQL结构
				for (Map.Entry<String, String> entry : columnMap.entrySet()) {
					String name = entry.getValue();
					String key = entry.getKey();
					if (notFields != null && (notFields.containsKey(name) || notFields.containsKey(key))) {
						continue;
					}
					String columnName = tableAsName + "." + name;
					if (name.equals(key)) {
						column.add(columnName);
					} else {
						column.add(columnName + " as " + key);
					}
				}
			}
			

			List<Object[]> childLeftJoinParams = leftJoinParam.getLeftJoinParams();
			if (childLeftJoinParams != null && !childLeftJoinParams.isEmpty()) {
				this.getLeftJoinSelectColumn(childLeftJoinParams, column);
			}
		}
	}

	private void getSelectColumn(String tableAsName, List<String> column, List<Object[]> fields, Map<String, String> fieldMap,
			Map<String, String> columnMap, Map<String, String> notFields) {
		// 别名加点
		if (!ValidateTool.isEmpty(tableAsName) && !tableAsName.contains(".")) {
			tableAsName += ".";
		}
		for (Object[] obj : fields) {
			String fieldName = obj[0].toString();
			Object alia = obj[2];

			/**
			 * HuangLongPu 于2019-01-31进行修复，此前当聚合函数的字段于别名相同时会存在bug
			 */
			String fieldTemp = null;
			if (columnMap.containsKey(fieldName)) {
				fieldTemp = columnMap.get(fieldName);
			} else {
				fieldTemp = fieldName;
			}
			String fieldAlia = ValidateTool.isEmpty(alia) ? "" : alia.toString();
			if (ValidateTool.isEmpty(fieldAlia) || (fieldMap.containsKey(fieldTemp) && !columnMap.containsKey(fieldAlia))) {
				// 说明是注解类型的字段
				fieldAlia = fieldMap.get(fieldTemp);
			}

			if (notFields != null && (notFields.containsKey(fieldAlia) || notFields.containsKey(fieldName) || notFields.containsKey(fieldTemp))) {
				continue;
			}
			SqlHandleEnum type = (SqlHandleEnum) obj[1];
			String columnName = null;
			String fieldAsTemp = ValidateTool.isEmpty(fieldAlia) ? "" : " as " + fieldAlia;
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
				columnName = "sum(" + getAgFunction(tableAsName, fieldTemp) + ")";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_MAX:
				columnName = "max(" + getAgFunction(tableAsName, fieldTemp) + ")";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_MIN:
				columnName = "min(" + getAgFunction(tableAsName, fieldTemp) + ")";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_AVG:
				columnName = "avg(" + getAgFunction(tableAsName, fieldTemp) + ")";
				column.add(columnName + fieldAsTemp);
				break;
			case HANDLE_EXP:
				columnName = getAgFunction(tableAsName, fieldTemp);
				column.add(columnName + fieldAsTemp);
				break;
			default:
				if (!fieldMap.containsKey(fieldTemp) && !columnMap.containsKey(fieldTemp)) {
					throw new HandleException("error: fieldName('" + fieldName + "')  is invalid！！！");
				} else {
					columnName = tableAsName + fieldTemp;
					column.add(columnName + fieldAsTemp);
				}
				break;
			}
			// } else {
			// if (fieldMap.containsKey(fieldName)) {
			// String fieldAlia = fieldMap.find(fieldName);
			// if (notFields != null && (notFields.containsKey(fieldName) ||
			// notFields.containsKey(fieldAlia))) {
			// continue;
			// }
			// String columnName = tableAsName + fieldName;
			// column.add(columnName + " as " + fieldAlia);
			// } else if (columnMap.containsKey(fieldName)) {
			// String columnName = columnMap.find(fieldName);
			// if (notFields != null && (notFields.containsKey(columnName) ||
			// notFields.containsKey(fieldName))) {
			// continue;
			// }
			// if (columnName.equals(fieldName)) {
			// column.add(tableAsName + fieldName + " as " + fieldName);
			// } else {
			// column.add(tableAsName + fieldName);
			// }
			// } else {
			// throw new HandleException("error: fieldName('" + fieldName +
			// "')  is invalid！！！");
			// }
			// }
		}
	}

	/**
	 * 解析聚合函数，拼装SQL
	 * 
	 * @param tableAsName
	 * @param fieldName
	 * @return
	 */
	private String getAgFunction(String tableAsName, String fieldName) {
		boolean replaceFlag = false;
		String fieldNameTemp = fieldName;
		if (fieldName.contains("+")) {
			fieldName = fieldName.replace("+", "}+{");
			fieldNameTemp = fieldNameTemp.replace("+", ",");
			if (!replaceFlag) {
				replaceFlag = true;
			}
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
		// ((a + b +c)-(b+c))
		//

		// String subTableName = tableAsName.contains(".") ? tableAsName :
		// tableAsName + ".";
		if (replaceFlag) {
			fieldName = fieldName.replaceAll(" ", "");
			fieldNameTemp = fieldNameTemp.replaceAll(" ", "");
			fieldName = "{" + fieldName + "}";
			String[] fieldNameTempArr = fieldNameTemp.split(",");
			Map<String, String> fieldNameTempMap = new HashMap<String, String>();
			for (String name : fieldNameTempArr) {
				if (ValidateTool.isEmpty(name)) {
					continue;
				}
				fieldNameTempMap.put(name, name);
			}

			for (Map.Entry<String, String> map : fieldNameTempMap.entrySet()) {
				String field = map.getValue();
				fieldName = fieldName.replace("{" + field + "}", tableAsName + field);
			}

			return fieldName.replaceAll("[{}]", "");
		} else {
			return tableAsName + fieldName;
		}
	}

	/**
	 * 根据业务传入的type值，判断条件类型
	 *
	 * @param type
	 * @return
	 */
	protected String getFilterType(FilterEnum type) {

		String filterType = null;
		switch (type) {
		case FILTER_LIKE:
			filterType = " like ";
			break;
		case FILTER_LEFT_LIKE:
			filterType = " like ";
			break;
		case FILTER_RIGHT_LIKE:
			filterType = " like ";
			break;
		case FILTER_EQUAL:
			filterType =  " = ";
			break;
		case FILTER_GREATETHAN:
			filterType = " > ";
			break;
		case FILTER_GREATEEQUAL:
			filterType = " >= ";
			break;
		case FILTER_LESSTHAN:
			filterType = " < ";
			break;
		case FILTER_LESSEQUAL:
			filterType = " <= ";
			break;
		case FILTER_NOTEQUAL:
			filterType = " <> ";
			break;
		case FILTER_IN:
			filterType = " in ";
			break;
		case FILTER_NOTIN:
			filterType = " not in ";
			break;
		case FILTER_ISNULL:
			filterType = " is null ";
			break;
		case FILTER_ISNOTNULL:
			filterType = " is not null ";
			break;
		default:
			break;
		}

		return filterType;
	}

	public void getQueryPageSql(Map<String, Object> param, String tableName) {

		SQL sql = new SQL();
		QueryProvider QueryProvider = (QueryProvider) param.get(SqlConstant.PARAM_OBJ);
		Map<String, String> columnMap = CacheInfoConstant.COLUMN_CACHE.get(tableName);
		Map<String, String> fieldMap = CacheInfoConstant.FIELD_CACHE.get(tableName);
		// int len = CacheInfoConstant.COLUMN_SIZE.find(tableName);
		String tableAsName = TableNameConvert.getTableAsName(tableName);
		sql.SELECT(getSelectFieldColumns(QueryProvider, tableAsName, columnMap, fieldMap));
		String table = tableName + " " + tableAsName + getLeftJoinTable(tableAsName, QueryProvider.getLeftJoinParams());
		sql.FROM(table);
		// 分页的语句
		SQL countSql = new SQL();
		countSql.SELECT("count(1)");
		countSql.FROM(table);

		// 构造 group by 语句
		List<String> groups = new ArrayList<String>();
		// 构造order by 语句
		List<String> orders = new ArrayList<String>();
		this.getGroupBy(groups, tableAsName, columnMap, QueryProvider);
		this.getOrder(orders, tableAsName, columnMap, QueryProvider);

		List<Object[]> filters = QueryProvider.getFilters();
		if ((filters != null && !filters.isEmpty()) || (QueryProvider.getLeftJoinParams() != null && !QueryProvider.getLeftJoinParams().isEmpty())) {
			Map<String, Object> value = new HashMap<String, Object>();
			String filterSql = getFilterSql(QueryProvider.getLeftJoinParams(), groups, orders, tableAsName, filters, QueryProvider.getOrParams(), value,
					INDEX_DEFAULT, columnMap, fieldMap, DEFAULT_FIND);
			if (!ValidateTool.isEmpty(filterSql)) {
				// 放入值到map
				param.put(SqlConstant.PARAM_FILTER, value);
				sql.WHERE(filterSql);
				countSql.WHERE(filterSql);
			}
		}

		if (!groups.isEmpty()) {
			sql.GROUP_BY(groups.toArray(new String[groups.size()]));
			countSql.GROUP_BY(groups.toArray(new String[groups.size()]));
		}
		
		if (!orders.isEmpty()) {
			sql.ORDER_BY(orders.toArray(new String[orders.size()]));
		}

		if (PageEnum.IS_PAGE_TRUE.equals(QueryProvider.getIsPage())) {
			if (groups != null && !groups.isEmpty()) {
				param.put(SqlConstant.COUNT_SQL, "select count(1) from (" + countSql.toString() + ") s");
			} else {
				param.put(SqlConstant.COUNT_SQL, countSql.toString());
			}
		}

		param.put(SqlConstant.QUERY_SQL, sql.toString());
	}

	/**
	 * 获取like sql
	 * 
	 * @param expression
	 *            表达式
	 * @return String
	 */
	abstract protected String getLikeSql(String expression);

	/**
	 * 获取左like sql
	 * 
	 * @param expression
	 *            表达式
	 * @return String
	 */
	abstract protected String getLeftLikeSql(String expression);

	/**
	 * 获取右like sql
	 * 
	 * @param expression
	 *            表达式
	 * @return String
	 */
	abstract protected String getRightLikeSql(String expression);

	/**
	 * 增加分页
	 * 
	 * @param sql
	 *            原sql
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            当前页数量
	 * @param reset
	 *            是否重置页码
	 * @return String
	 */
	abstract protected String appendPageSql(String sql, int pageNo, int pageSize, boolean reset);

	protected int getIndexPage(int pageNo, int pageSize) {
		return (pageNo - 1) * pageSize;
	}

	protected int getLastPage(int pageNo, int pageSize) {
		return pageNo * pageSize;
	}

}
