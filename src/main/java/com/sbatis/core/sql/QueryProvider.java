package com.sbatis.core.sql;

import com.sbatis.config.request.PageParam;
import com.sbatis.config.request.RequestConstant;
import com.sbatis.config.request.RequestParam;
import com.sbatis.core.constant.type.FilterEnum;
import com.sbatis.core.constant.type.OrderEnum;
import com.sbatis.core.constant.type.PageEnum;
import com.sbatis.core.constant.type.SqlHandleEnum;
import com.sbatis.core.exception.HandleException;
import com.sbatis.core.result.ResultInfoOutput;
import com.sbatis.core.convert.BeanConvert;
import com.sbatis.validate.ValidateTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作 sql 封装操作类，除使用直接拼装 sql 外，其余数据库操作全部使用这个类提供的属性进行操作
 * @author HuangLongPu
 */
public class QueryProvider {

	protected static final String JOIN_AND_EXPRESS = " and ";
	protected static final String JOIN_OR_EXPRESS = " or ";

	protected static AbstractOrder abstractOrder;

	private static final Map<Integer, OrderEnum> ORDER_TYPE_MAP = new HashMap<>();

	static {
		// 加载排序方式和值
		ORDER_TYPE_MAP.put(RequestConstant.ORDER_ASC, OrderEnum.ORDER_ASC);
		ORDER_TYPE_MAP.put(RequestConstant.ORDER_DESC, OrderEnum.ORDER_DESC);
	}

	private int pageNumber = RequestConstant.DEFAULT_PAGE;
	private int pageSize = RequestConstant.DEFAULT_ROWS;
	private PageEnum isPage = PageEnum.IS_PAGE_FALSE;
	private boolean resetIndexPage = false;

	private List<Object[]> fields;
	private List<Object[]> filters;
	private List<String[]> orders;
	private List<String> groups;
	private List<QueryProvider> orProviders;
	private Map<String, String> notFields;
	private List<Object[]> leftJoinProviders;
	private String joinTableName;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public PageEnum getIsPage() {
		return isPage;
	}

	public void setIsPage(PageEnum isPage) {
		this.isPage = isPage;
	}

	public void setPage(PageParam pageParam) {
		this.setPageNumber(pageParam.getPage());
		this.setPageSize(pageParam.getRows());

		String sort = pageParam.getSort();
		if (!ValidateTool.isEmpty(sort)) {
			this.addOrder(sort, ORDER_TYPE_MAP.get(pageParam.getOrder()));
		}
	}

	public boolean isResetIndexPage() {
		return resetIndexPage;
	}

	public void setResetIndexPage(boolean resetIndexPage) {
		this.resetIndexPage = resetIndexPage;
	}

	public List<Object[]> getFields() {
		return fields;
	}

	public List<Object[]> getFilters() {
		return filters;
	}

	public List<String[]> getOrders() {
		return orders;
	}

	public List<String> getGroups() {
		return groups;
	}

	public List<QueryProvider> getOrProviders() {
		return orProviders;
	}

	public Map<String, String> getNotFields() {
		return notFields;
	}

	public List<Object[]> getLeftJoinProviders() {
		return leftJoinProviders;
	}

	protected String getJoinTableName() {
		return joinTableName;
	}

	/**
	 * 设置连接查询时 QueryProvider 属性表名，如果只是简单常规单表查询，即使设置了也无效。 目前主要支持 left join
	 * @author HuangLongPu
	 * @param joinTableName
	 */
	public void setJoinTableName(String joinTableName) {
		if (ValidateTool.isEmpty(joinTableName)) {
			throw new HandleException("error: joinTableName is null");
		}
		this.joinTableName = joinTableName;
	}

	/**
	 * 添加字段方法，接收一个参数，此方法主要用于查询 传入的值表示为要查询的字段名称
	 * @author HuangLongPu
	 * @param fieldName
	 * @throws HandleException
	 */
	public void addField(String fieldName) throws HandleException {
		this.addField(fieldName, null);
	}

	/**
	 * 添加字段方法，接收两个参数，此方法主要用于查询(select)或者修改(update) 此方法用于查询或者修改
	 * 用于查询时，第一个参数为要查询的字段名称，第二个参数可为null或者为要查询的别名，类似sql语句中的as name
	 * 用于修改时，第一个参数为要修改的字段名称，第二个为修改后的值
	 * @@author HuangLongPu
	 * @param fieldName
	 * @param value
	 * @throws HandleException
	 */
	public void addField(String fieldName, Object value) throws HandleException {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_DEFAULT, value);
	}

	/**
	 * 实现累加，比如money = money + 20类似的SQL语句; fieldName 表示要操作的字段名称,value 表示要操作的值
	 * @author HuangLongPu
	 * @param fieldName
	 * @param value
	 */
	public void addFieldUp(String fieldName, Object value) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_UP, value);
	}

	/**
	 * 实现累加，比如money = money - 20类似的SQL语句; fieldName 表示要操作的字段名称,value 表示要操作的值
	 * @author HuangLongPu
	 * @param fieldName
	 * @param value
	 */
	public void addFieldReduce(String fieldName, Object value) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_REDUCE, value);
	}
	
	/**
	 * count 统计函数 >> count(1)
	 * @author HuangLongPu
	 */
	public void addFieldCount() {
		this.addFieldCount("");
	}
	
	/**
	 * count 统计函数 >> count(1) as 'aliasName'
	 * @author HuangLongPu
	 * @param aliasName
	 */
	public void addFieldCount(String  aliasName) {
		this.addFieldValue("", SqlHandleEnum.HANDLE_COUNT, aliasName);
	}
	
	/**
	 * distinct 去重函数 >> distinct 'fieldName'
	 * @author HuangLongPu
	 * @param fieldName
	 */
	public void addFieldCountDistinct(String fieldName) {
		if(ValidateTool.isEmpty(fieldName)) {
			throw new HandleException("error:field is null");
		}
		this.addFieldCountDistinct(fieldName, "");
	}
	
	/**
	 * distinct 去重函数 >> distinct 'fieldName' as 'aliasName'
	 * @author HuangLongPu
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldCountDistinct(String fieldName, String aliasName) {
		if(ValidateTool.isEmpty(fieldName)) {
			throw new HandleException("error: field is null");
		}
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_COUNT, aliasName);
	}
	
	/**
	 * sum 求和函数 >> sum('fieldName')
	 * @author HuangLongPu
	 * @param fieldName
	 */
	public void addFieldSum(String fieldName) {
		this.addFieldSum(fieldName, null);
	}
	
	/**
	 * sum 求和函数 >> sum('fieldName') as 'aliasName'
	 * @author HuangLongPu
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldSum(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_SUM, aliasName);
	}
	
	/**
	 * min 最小值函数 >> min('fieldName')
	 * @author HuangLongPu
	 * @param fieldName
	 */
	public void addFieldMin(String fieldName) {
		this.addFieldMin(fieldName, null);
	}
	
	/**
	 * min 最小值函数 >> min('fieldName') as 'aliasName'
	 * @author HuangLongPu
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldMin(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_MIN, aliasName);
	}
	
	/**
	 * max 最大值函数 >> max('fieldName')
	 * @author HuangLongPu
	 * @param fieldName
	 */
	public void addFieldMax(String fieldName) {
		this.addFieldMax(fieldName, null);
	}
	
	/**
	 * max 最大值函数 >> max('fieldName') as 'aliasName'
	 * @author HuangLongPu
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldMax(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_MAX, aliasName);
	}
	
	/**
	 * avg 平均值函数 >> avg('fieldName')
	 * @author HuangLongPu
	 * @param fieldName
	 */
	public void addFieldAvg(String fieldName) {
		this.addFieldAvg(fieldName, null);
	}
	
	/**
	 * avg 平均值函数 >> avg('fieldName') as 'aliasName'
	 * @author HuangLongPu
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldAvg(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_AVG, aliasName);
	}
	
	/**
	 * 表达式函数，非聚合函数时使用，如需聚合，直接使用提供的聚合函数方法即可，同等原理
	 * @author HuangLongPu
	 * @param fieldName
	 */
	public void addFieldExp(String fieldName) {
		this.addFieldExp(fieldName, null);
	}
	
	/**
	 * 表达式函数，非聚合函数时使用，如需聚合，直接使用提供的聚合函数方法即可，同等原理
	 * @author HuangLongPu
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldExp(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_EXP, aliasName);
	}

	/**
	 * 设置表达式属性
	 * @author HuangLongPu
	 * @param fieldName
	 * @param fieldType
	 * @param value
	 */
	private void addFieldValue(String fieldName, SqlHandleEnum fieldType, Object value) {
		if (ValidateTool.isEmpty(fieldName) && !SqlHandleEnum.HANDLE_COUNT.equals(fieldType)) {
			throw new HandleException("error: field is null");
		}
		if (this.fields == null) {
			this.fields = new ArrayList<>();
		}
		Object[] obj = { fieldName, fieldType, value };
		this.fields.add(obj);
	}

	/**
	 * 添加不需要查询的字段，主要针对实体泛型返回的查询中，如果字段被加入，则会在 SQL 中过滤。
	 * @author HuangLongPu
	 * @param fieldName
	 */
	public void addNotField(String fieldName) {
		if (ValidateTool.isEmpty(fieldName)) {
			throw new HandleException("error: field is null");
		}
		if (this.notFields == null) {
			this.notFields = new HashMap<>();
		}
		this.notFields.put(fieldName, fieldName);
	}

	/**
	 * 添加查询条件，where后的字段;
	 * 参数分别为字段名称，比如name。条件类型，比如=，具体的值参考QueryParam的FILTER开头的常量。值
	 * ，比如张三。一起即可name='张三'; 该方法已过期，已由新的方法（addFilter*）替代，将会在后期版本中移除该方法;
	 * @author HuangLongPu
	 * @param filterName
	 * @param filterType
	 * @param value
	 */
	private void andFilter(String filterName, FilterEnum filterType, Object value) {
		this.addFilter(filterName, filterType, value, JOIN_AND_EXPRESS);
	}

	/**
	 * 设置or 查询条件数据
	 * @author HuangLongPu
	 * @param filterName
	 * @param filterType
	 * @param value
	 */
	private void orFilter(String filterName, FilterEnum filterType, Object value) {
		this.addFilter(filterName, filterType, value, JOIN_OR_EXPRESS);
	}

	/**
	 * 设置条件
	 * @author HuangLongPu
	 * @param filterName
	 * @param filterType
	 * @param value
	 * @param joinType
	 */
	private void addFilter(String filterName, FilterEnum filterType, Object value, String joinType) {
		if (ValidateTool.isEmpty(filterName)) {
			throw new HandleException("error: filter field is null");
		} else if (FilterEnum.FILTER_ISNULL.equals(filterType) && FilterEnum.FILTER_ISNOTNULL.equals(filterType) && null == value) {
			throw new HandleException("error: field is null");
		}
		if (this.filters == null) {
			this.filters = new ArrayList<>();
		}
		Object[] obj = {filterName, filterType, value, joinType};
		this.filters.add(obj);
	}

	/**
	 * 增加 and 查询条件，模糊查询, like
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterLike(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LIKE, value);
	}

	/**
	 * 增加 or 查询条件，模糊查询, like
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterLike(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LIKE, value);
	}

	/**
	 * 增加 and 查询条件，左模糊查询, like
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterLeftLike(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LEFT_LIKE, value);
	}

	/**
	 * 增加 or 查询条件，左模糊查询, like
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterLeftLike(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LEFT_LIKE, value);
	}

	/**
	 * 增加 and 查询条件，右模糊查询, like
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterRightLike(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_RIGHT_LIKE, value);
	}

	/**
	 * 增加 or 查询条件，右模糊查询, like
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterRightLike(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_RIGHT_LIKE, value);
	}

	/**
	 * 增加 and 查询条件，等于查询，=
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterEquals(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_EQUAL, value);
	}

	/**
	 * 增加 or 查询条件，等于查询，=
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterEquals(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_EQUAL, value);
	}

	/**
	 * 增加 and 查询条件，大于查询，>
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterGreateThan(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_GREATETHAN, value);
	}

	/**
	 * 增加 or 查询条件，大于查询，>
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterGreateThan(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_GREATETHAN, value);
	}

	/**
	 * 增加 and 查询条件，大于等于查询，>=
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterGreateEqual(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, value);
	}

	/**
	 * 增加 or 查询条件，大于等于查询，>=
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterGreateEqual(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, value);
	}

	/**
	 * 增加 and 大于等于0的条件表达式，传入字段名称即可
	 * @author HuangLongPu
	 * @param filterName
	 */
	public void addFilterGreateEqualZero(String filterName) {
		this.andFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, 0);
	}

	/**
	 * 增加 or 大于等于0的条件表达式，传入字段名称即可
	 * @author HuangLongPu
	 * @param filterName
	 */
	public void orFilterGreateEqualZero(String filterName) {
		this.orFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, 0);
	}

	/**
	 * 增加 and 查询条件，小于查询，<
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterLessThan(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LESSTHAN, value);
	}

	/**
	 * 增加 or 查询条件，小于查询，<
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterLessThan(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LESSTHAN, value);
	}

	/**
	 * 增加 and 查询条件，小于等于查询，<=
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterLessEqual(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LESSEQUAL, value);
	}

	/**
	 * 增加 or 查询条件，小于等于查询，<=
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterLessEqual(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LESSEQUAL, value);
	}

	/**
	 * 增加 and 查询，不等于查询，<>
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterNotEqual(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_NOTEQUAL, value);
	}

	/**
	 * 增加 or 查询，不等于查询，<>
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterNotEqual(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_NOTEQUAL, value);
	}

	/**
	 * 增加 and 查询条件，属于查询，in
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterIn(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_IN, value);
	}

	/**
	 * 增加 or 查询条件，属于查询，in
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterIn(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_IN, value);
	}

	/**
	 * 增加 and 查询条件，不属于查询，not in
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterNotIn(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_NOTIN, value);
	}

	/**
	 * 增加 or 查询条件，不属于查询，not in
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterNotIn(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_NOTIN, value);
	}

	/**
	 * 增加 and 查询条件，表示null值查询，is null
	 * @author HuangLongPu
	 * @param filterName
	 */
	public void addFilterIsNull(String filterName) {
		this.andFilter(filterName, FilterEnum.FILTER_ISNULL, null);
	}

	/**
	 * 增加 or 查询条件，表示null值查询，is null
	 * @author HuangLongPu
	 * @param filterName
	 */
	public void orFilterIsNull(String filterName) {
		this.orFilter(filterName, FilterEnum.FILTER_ISNULL, null);
	}

	/**
	 * 增加 and 查询条件，表示null值查询，is not null
	 * @author HuangLongPu
	 * @param filterName
	 */
	public void addFilterIsNotNull(String filterName) {
		this.andFilter(filterName, FilterEnum.FILTER_ISNOTNULL, null);
	}

	/**
	 * 增加 or 查询条件，表示null值查询，is not null
	 * @author HuangLongPu
	 * @param filterName
	 */
	public void orFilterIsNotNull(String filterName) {
		this.orFilter(filterName, FilterEnum.FILTER_ISNOTNULL, null);
	}

	/**
	 * 增加 and 设定值后大于条件判断，比如count + 10 > 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterUpGreateThanZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_UPGREATETHAN, value);
	}

	/**
	 * 增加 or 设定值后大于条件判断，比如count + 10 > 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterUpGreateThanZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_UPGREATETHAN, value);
	}

	/**
	 * 增加 and 设定值后大于等于条件判断，比如count + 10 >= 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterUpGreateEqualZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_UPGREATEEQUAL, value);
	}

	/**
	 * 增加 or 设定值后大于等于条件判断，比如count + 10 >= 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterUpGreateEqualZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_UPGREATEEQUAL, value);
	}

	/**
	 * 增加 and 设定值后大于条件判断，比如count + 10 > 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterReduceGreateThanZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_REDUCEGREATETHAN, value);
	}

	/**
	 * 增加 or 设定值后大于条件判断，比如count + 10 > 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterReduceGreateThanZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_REDUCEGREATETHAN, value);
	}

	/**
	 * 减少 and 设定值后小于等于条件判断，比如count - 10 >= 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void addFilterReduceGreateEqualZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_REDUCEGREATEEQUAL, value);
	}

	/**
	 * 减少 or 设定值后小于等于条件判断，比如count - 10 >= 0
	 * @author HuangLongPu
	 * @param filterName
	 * @param value
	 */
	public void orFilterReduceGreateEqualZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_REDUCEGREATEEQUAL, value);
	}

	/**
	 * 添加查询添加，比如 and (type = 1 or name = 2)，主要作用于拼接 and 后括号中的表达式，主要用于 or
	 * 查询的表达式，不然没必要。 从 V2.2.5.8 版本开始，该方法已被弃用，由方法 addOrProvider 替代
	 * @author HuangLongPu
	 * @param param
	 */
	@Deprecated
	public void addParam(QueryProvider param) {
		if (param == null) {
			throw new HandleException("error: request is null");
		}

		if (this.orProviders == null) {
			orProviders = new ArrayList<>();
		}

		this.orProviders.add(param);
	}

	/**
	 * 添加查询添加，比如 and (type = 1 or name = 2)，主要作用于拼接 and 后括号中的表达式，主要用于 or
	 * 查询的表达式，不然没必要。 如果 多条件拼接 or 查询(类似 where id = ? and name = ? or type = 1
	 * 的条件)，or 条件查询不能被当成第一个条件放入(type属性 orFilter 方法不能在第一个加入)，否则会被解析为 and 条件查询。 V
	 * @author HuangLongPu
	 * @param queryProvider
	 */
	public void addOrProvider(QueryProvider queryProvider) {
		if (queryProvider == null) {
			throw new HandleException("error: queryProvider is null");
		}

		if (this.orProviders == null) {
			orProviders = new ArrayList<>();
		}

		this.orProviders.add(queryProvider);
	}

	/**
	 * 添加 left join 查询，会被拼接到left join 的连体SQL。 当使用这个属性时，必须设置 joinTableName的连接表名。
	 * @author HuangLongPu
	 * @param fieldName        表示left join 前面一张关联字段。
	 * @param paramFieldName   表示left join 后紧跟表的关联字段。
	 * @param queryProvider    被left join的封装对象。
	 */
	public void addLeftJoinProvider(String fieldName, String paramFieldName, QueryProvider queryProvider) {
		if (fieldName == null) {
			throw new HandleException("error: left join fieldName is null");
		}
		if (paramFieldName == null) {
			throw new HandleException("error: left join paramFieldName is null");
		}
		if (queryProvider == null) {
			throw new HandleException("error: queryProvider can't null");
		}
		if (ValidateTool.isEmpty(queryProvider.getJoinTableName())) {
			throw new HandleException("error: queryProvider joinTableName is null");
		}

		if (this.leftJoinProviders == null) {
			leftJoinProviders = new ArrayList<>();
		}

		Object[] obj = { fieldName, paramFieldName, queryProvider };
		this.leftJoinProviders.add(obj);
	}

	/**
	 * 添加 left join 查询，会被拼接到left join 的连体SQL。 当使用这个属性时，必须设置 joinTableName
	 * 的连接表名。 针对多条件，两数组长度必须一致。
	 * @author HuangLongPu
	 * @param fieldName         表示left join 前面一张关联字段。
	 * @param paramFieldName    表示left join 后紧跟表的关联字段。
	 * @param queryProvider             被left join的封装对象。
	 */
	public void addLeftJoinProvider(String[] fieldName, String[] paramFieldName, QueryProvider queryProvider) {
		int fieldLength = 0;
		if (fieldName == null || (fieldLength = fieldName.length) == 0) {
			throw new HandleException("error: left join fieldName is null");
		}
		int paramFieldLength = 0;
		if (paramFieldName == null || (paramFieldLength = paramFieldName.length) == 0) {
			throw new HandleException("error: left join paramFieldName is null");
		}
		if (fieldLength != paramFieldLength) {
			throw new HandleException("error: left join 'on' filter length must be equal");
		}
		if (queryProvider == null) {
			throw new HandleException("error: queryProvider is null");
		}

		if (this.leftJoinProviders == null) {
			leftJoinProviders = new ArrayList<>();
		}

		Object[] obj = { fieldName, paramFieldName, queryProvider };
		this.leftJoinProviders.add(obj);
	}

	/**
	 * 增加排序，参数分别为排序字段，排序值，排序值类型参考QueryParam中ORDER开头的常量
	 * @author HuangLongPu
	 * @param orderName
	 * @param orderType
	 */
	public void addOrder(String orderName, OrderEnum orderType) {
		if (ValidateTool.isEmpty(orderName)) {
			throw new HandleException("error: order field is null");
		}

		if (this.orders == null) {
			this.orders = new ArrayList<>();
		}
		abstractOrder.addOrder(orders, orderName, orderType);
	}

	/**
	 * 增加分组，根据字段名称进行分组
	 * @author HuangLongPu
	 * @param groupName
	 */
	public void addGroup(String groupName) {
		if (ValidateTool.isEmpty(groupName)) {
			throw new HandleException("error: group field is null");
		}
		if (this.groups == null) {
			this.groups = new ArrayList<>();
		}
		this.groups.add(groupName);
	}

	/**
	 * 移除所有查询条件
	 * @author HuangLongPu
	 * @param
	 */
	public void removeFilter() {
		this.filters.clear();
	}

	/**
	 * 根据前端传入的 command 实体，获取查询属性的 @QueryFilter 注解值
	 * @author HuangLongPu
	 * @param obj
	 */
	public void setFilters(Object obj) {
		if (!(obj instanceof RequestParam)) {
			throw new HandleException("error: the filter is not instanceof RequestQueryParam");
		}
		QueryHandle.getFilters(obj, this);
	}

	/**
	 * 根据前端传入的 command 实体，获取修改属性的 @UpdateField 注解值
	 * @author HuangLongPu
	 * @param obj
	 */
	public void setUpdateField(Object obj) {
		if (!(obj instanceof RequestParam)) {
			throw new HandleException("error: the update is not instanceof RequestQueryParam");
		}
		QueryHandle.getUpdateField(obj, this);
	}
	
	/**
	 * 传入 ResultInfoOutput 的子类进行自动转换。
	 * 如果接收的属性与数据库字段不一致，用@Column 注解映射，映射可以是实体属性名和字段名。
	 * 如果有属性不想被添加到addField中，用@NotColumn 注解映射，将会自动过滤。
	 * @author HuangLongPu
	 * @param cls
	 */
	public void setFieldColumn(Class<?> cls) {
		if(!ResultInfoOutput.class.isAssignableFrom(cls)) {
			throw new HandleException("error: the select is not instanceof ResultInfoOutput");
		}
		
		List<String[]> result = BeanConvert.getResultFields(cls);
		for (String[] field : result) {
			this.addField(field[0], field[1]);
		}
	}

}
