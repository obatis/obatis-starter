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
import com.sbatis.core.sql.param.bigint.AbstractColumnParam;
import com.sbatis.core.sql.param.bigint.IntParam;
import com.sbatis.core.util.BeanConvert;
import com.sbatis.core.util.QueryConvert;
import com.sbatis.validate.ValidateTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作 sql 封装操作类
 * @author
 */
public class QueryProvider {

	protected static final String JOIN_AND_EXPRESS = " and ";
	protected static final String JOIN_OR_EXPRESS = " or ";

	protected static AbstractColumnParam columnParam = null;
	protected static AbstractOrder abstractOrder;

	private static final Map<Integer, OrderEnum> ORDER_TYPE_MAP = new HashMap<Integer, OrderEnum>();

	static {
		// 加载排序方式和值
		ORDER_TYPE_MAP.put(RequestConstant.ORDER_ASC, OrderEnum.ORDER_ASC);
		ORDER_TYPE_MAP.put(RequestConstant.ORDER_DESC, OrderEnum.ORDER_DESC);
	}

	private int indexPage = RequestConstant.DEFAULT_PAGE;
	private int pageSize = RequestConstant.DEFAULT_ROWS;
	private PageEnum isPage = PageEnum.IS_PAGE_FALSE;
	private boolean resetIndexPage = false;

	private List<Object[]> fields;
	private List<Object[]> filters;
	private List<String[]> orders;
	private List<String> groups;
	private List<QueryProvider> orParams;
	private Map<String, String> notFields;
	private List<Object[]> leftJoinParams;
	private String connectTableName;

	public int getIndexPage() {
		return indexPage;
	}

	public void setIndexPage(int indexPage) {
		this.indexPage = indexPage;
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

	public void setPage(PageParam query) {
		this.setIndexPage(query.getPage());
		this.setPageSize(query.getRows());

		String sort = query.getSort();
		if (!ValidateTool.isEmpty(sort)) {
			this.addOrder(sort, ORDER_TYPE_MAP.get(query.getOrder()));
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

	public List<QueryProvider> getOrParams() {
		return orParams;
	}

	public Map<String, String> getNotFields() {
		return notFields;
	}

	public List<Object[]> getLeftJoinParams() {
		return leftJoinParams;
	}

	protected String getConnectTableName() {
		return connectTableName;
	}

	/**
	 * 设置连接查询时 QueryProvider 属性表名，如果只是简单常规单表查询，即使设置了也无效。 目前主要支持 left join
	 * 
	 * @param connectTableName
	 */
	public void setConnectTableName(String connectTableName) {
		if (ValidateTool.isEmpty(connectTableName)) {
			throw new HandleException("set connectTableName Error:connectTableName can't null(empty)!!!");
		}
		this.connectTableName = connectTableName;
	}

	/**
	 * 添加字段方法，接收一个参数，此方法主要用于查询 传入的值表示为要查询的字段名称
	 * 
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
	 * 
	 * @param fieldName
	 * @param value
	 * @throws HandleException
	 */
	public void addField(String fieldName, Object value) throws HandleException {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_DEFAULT, value);
	}

	/**
	 * 实现累加，比如money = money + 20类似的SQL语句; fieldName 表示要操作的字段名称,value 表示要操作的值
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void addFieldUp(String fieldName, Object value) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_UP, value);
	}

	/**
	 * 实现累加，比如money = money - 20类似的SQL语句; fieldName 表示要操作的字段名称,value 表示要操作的值
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void addFieldReduce(String fieldName, Object value) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_REDUCE, value);
	}
	
	/**
	 * count 统计函数 >> count(1)
	 */
	public void addFieldCount() {
		this.addFieldCount("");
	}
	
	/**
	 * count 统计函数 >> count(1) as 'aliasName'
	 * @param aliasName
	 */
	public void addFieldCount(String  aliasName) {
		this.addFieldValue("", SqlHandleEnum.HANDLE_COUNT, aliasName);
	}
	
	/**
	 * distinct 去重函数 >> distinct 'fieldName'
	 * @param fieldName
	 */
	public void addFieldCountDistinct(String fieldName) {
		if(ValidateTool.isEmpty(fieldName)) {
			throw new HandleException("add field Error:field can't null!!!");
		}
		this.addFieldCountDistinct(fieldName, "");
	}
	
	/**
	 * distinct 去重函数 >> distinct 'fieldName' as 'aliasName'
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldCountDistinct(String fieldName, String aliasName) {
		if(ValidateTool.isEmpty(fieldName)) {
			throw new HandleException("add field Error:field can't null!!!");
		}
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_COUNT, aliasName);
	}
	
	/**
	 * sum 求和函数 >> sum('fieldName')
	 * @param fieldName
	 */
	public void addFieldSum(String fieldName) {
		this.addFieldSum(fieldName, null);
	}
	
	/**
	 * sum 求和函数 >> sum('fieldName') as 'aliasName'
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldSum(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_SUM, aliasName);
	}
	
	/**
	 * min 最小值函数 >> min('fieldName')
	 * @param fieldName
	 */
	public void addFieldMin(String fieldName) {
		this.addFieldMin(fieldName, null);
	}
	
	/**
	 * min 最小值函数 >> min('fieldName') as 'aliasName'
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldMin(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_MIN, aliasName);
	}
	
	/**
	 * max 最大值函数 >> max('fieldName')
	 * @param fieldName
	 */
	public void addFieldMax(String fieldName) {
		this.addFieldMax(fieldName, null);
	}
	
	/**
	 * max 最大值函数 >> max('fieldName') as 'aliasName'
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldMax(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_MAX, aliasName);
	}
	
	/**
	 * avg 平均值函数 >> avg('fieldName')
	 * @param fieldName
	 */
	public void addFieldAvg(String fieldName) {
		this.addFieldAvg(fieldName, null);
	}
	
	/**
	 * avg 平均值函数 >> avg('fieldName') as 'aliasName'
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldAvg(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_AVG, aliasName);
	}
	
	/**
	 * 表达式函数，非聚合函数时使用，如需聚合，直接使用提供的聚合函数方法即可，同等原理
	 * @param fieldName
	 */
	public void addFieldExp(String fieldName) {
		this.addFieldExp(fieldName, null);
	}
	
	/**
	 * 表达式函数，非聚合函数时使用，如需聚合，直接使用提供的聚合函数方法即可，同等原理
	 * @param fieldName
	 * @param aliasName
	 */
	public void addFieldExp(String fieldName, String  aliasName) {
		this.addFieldValue(fieldName, SqlHandleEnum.HANDLE_EXP, aliasName);
	}

	private void addFieldValue(String fieldName, SqlHandleEnum fieldType, Object value) {
		if (ValidateTool.isEmpty(fieldName) && !SqlHandleEnum.HANDLE_COUNT.equals(fieldType)) {
			throw new HandleException("add field Error:field can't null!!!");
		}
		if (this.fields == null) {
			this.fields = new ArrayList<Object[]>();
		}
		Object[] obj = { fieldName, fieldType, value };
		this.fields.add(obj);
	}

	/**
	 * 添加不需要查询的字段，主要针对实体泛型返回的查询中，如果字段被加入，则会在 SQL 中过滤。
	 * 
	 * @param fieldName
	 */
	public void addNotField(String fieldName) {
		if (ValidateTool.isEmpty(fieldName)) {
			throw new HandleException("add field Error:field can't null!!!");
		}
		if (this.notFields == null) {
			this.notFields = new HashMap<String, String>();
		}
		this.notFields.put(fieldName, fieldName);
	}

	/**
	 * 添加查询条件，where后的字段;
	 * 参数分别为字段名称，比如name。条件类型，比如=，具体的值参考QueryParam的FILTER开头的常量。值
	 * ，比如张三。一起即可name='张三'; 该方法已过期，已由新的方法（addFilter*）替代，将会在后期版本中移除该方法;
	 * 
	 * @param filterName
	 * @param filterType
	 * @param value
	 */
	private void andFilter(String filterName, FilterEnum filterType, Object value) {
		this.addFilter(filterName, filterType, value, JOIN_AND_EXPRESS);
	}

	private void orFilter(String filterName, FilterEnum filterType, Object value) {
		this.addFilter(filterName, filterType, value, JOIN_OR_EXPRESS);
	}

	private void addFilter(String filterName, FilterEnum filterType, Object value, String joinType) {
		if (ValidateTool.isEmpty(filterName)) {
			throw new HandleException("add filter Error:filter field can't null!!!");
		} else if (FilterEnum.FILTER_ISNULL.equals(filterType) && FilterEnum.FILTER_ISNOTNULL.equals(filterType) && null == value) {
			throw new HandleException("add field value Error:field can't null!!!");
		}
		if (this.filters == null) {
			this.filters = new ArrayList<Object[]>();
		}
		// Object[] obj = {filterName, filterType, value};
		this.filters.add(this.getColumnParam().addFilter(filterName, filterType, value, joinType));
	}

	/**
	 * 增加 and 查询条件，模糊查询, like
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterLike(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LIKE, value);
	}

	/**
	 * 增加 or 查询条件，模糊查询, like
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterLike(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LIKE, value);
	}

	/**
	 * 增加 and 查询条件，左模糊查询, like
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterLeftLike(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LEFT_LIKE, value);
	}

	/**
	 * 增加 or 查询条件，左模糊查询, like
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterLeftLike(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LEFT_LIKE, value);
	}

	/**
	 * 增加 and 查询条件，右模糊查询, like
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterRightLike(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_RIGHT_LIKE, value);
	}

	/**
	 * 增加 or 查询条件，右模糊查询, like
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterRightLike(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_RIGHT_LIKE, value);
	}

	/**
	 * 增加 and 查询条件，等于查询，=
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterEquals(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_EQUAL, value);
	}

	/**
	 * 增加 or 查询条件，等于查询，=
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterEquals(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_EQUAL, value);
	}

	/**
	 * 增加 and 查询条件，大于查询，>
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterGreateThan(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_GREATETHAN, value);
	}

	/**
	 * 增加 or 查询条件，大于查询，>
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterGreateThan(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_GREATETHAN, value);
	}

	/**
	 * 增加 and 查询条件，大于等于查询，>=
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterGreateEqual(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, value);
	}

	/**
	 * 增加 or 查询条件，大于等于查询，>=
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterGreateEqual(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, value);
	}

	/**
	 * 增加 and 大于等于0的条件表达式，传入字段名称即可
	 * 
	 * @param filterName
	 */
	public void addFilterGreateEqualZero(String filterName) {
		this.andFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, 0);
	}

	/**
	 * 增加 or 大于等于0的条件表达式，传入字段名称即可
	 * 
	 * @param filterName
	 */
	public void orFilterGreateEqualZero(String filterName) {
		this.orFilter(filterName, FilterEnum.FILTER_GREATEEQUAL, 0);
	}

	/**
	 * 增加 and 查询条件，小于查询，<
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterLessThan(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LESSTHAN, value);
	}

	/**
	 * 增加 or 查询条件，小于查询，<
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterLessThan(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LESSTHAN, value);
	}

	/**
	 * 增加 and 查询条件，小于等于查询，<=
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterLessEqual(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_LESSEQUAL, value);
	}

	/**
	 * 增加 or 查询条件，小于等于查询，<=
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterLessEqual(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_LESSEQUAL, value);
	}

	/**
	 * 增加 and 查询，不等于查询，<>
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterNotEqual(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_NOTEQUAL, value);
	}

	/**
	 * 增加 or 查询，不等于查询，<>
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterNotEqual(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_NOTEQUAL, value);
	}

	/**
	 * 增加 and 查询条件，属于查询，in
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterIn(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_IN, value);
	}

	/**
	 * 增加 or 查询条件，属于查询，in
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterIn(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_IN, value);
	}

	/**
	 * 增加 and 查询条件，不属于查询，not in
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterNotIn(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_NOTIN, value);
	}

	/**
	 * 增加 or 查询条件，不属于查询，not in
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterNotIn(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_NOTIN, value);
	}

	/**
	 * 增加 and 查询条件，表示null值查询，is null
	 * 
	 * @param filterName
	 */
	public void addFilterIsNull(String filterName) {
		this.andFilter(filterName, FilterEnum.FILTER_ISNULL, null);
	}

	/**
	 * 增加 or 查询条件，表示null值查询，is null
	 * 
	 * @param filterName
	 */
	public void orFilterIsNull(String filterName) {
		this.orFilter(filterName, FilterEnum.FILTER_ISNULL, null);
	}

	/**
	 * 增加 and 查询条件，表示null值查询，is not null
	 * 
	 * @param filterName
	 */
	public void addFilterIsNotNull(String filterName) {
		this.andFilter(filterName, FilterEnum.FILTER_ISNOTNULL, null);
	}

	/**
	 * 增加 or 查询条件，表示null值查询，is not null
	 * 
	 * @param filterName
	 */
	public void orFilterIsNotNull(String filterName) {
		this.orFilter(filterName, FilterEnum.FILTER_ISNOTNULL, null);
	}

	/**
	 * 增加 and 设定值后大于条件判断，比如count + 10 > 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterUpGreateThanZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_UPGREATETHAN, value);
	}

	/**
	 * 增加 or 设定值后大于条件判断，比如count + 10 > 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterUpGreateThanZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_UPGREATETHAN, value);
	}

	/**
	 * 增加 and 设定值后大于等于条件判断，比如count + 10 >= 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterUpGreateEqualZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_UPGREATEEQUAL, value);
	}

	/**
	 * 增加 or 设定值后大于等于条件判断，比如count + 10 >= 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterUpGreateEqualZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_UPGREATEEQUAL, value);
	}

	/**
	 * 增加 and 设定值后大于条件判断，比如count + 10 > 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterReduceGreateThanZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_REDUCEGREATETHAN, value);
	}

	/**
	 * 增加 or 设定值后大于条件判断，比如count + 10 > 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterReduceGreateThanZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_REDUCEGREATETHAN, value);
	}

	/**
	 * 减少 and 设定值后小于等于条件判断，比如count - 10 >= 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void addFilterReduceGreateEqualZero(String filterName, Object value) {
		this.andFilter(filterName, FilterEnum.FILTER_REDUCEGREATEEQUAL, value);
	}

	/**
	 * 减少 or 设定值后小于等于条件判断，比如count - 10 >= 0
	 * 
	 * @param filterName
	 * @param value
	 */
	public void orFilterReduceGreateEqualZero(String filterName, Object value) {
		this.orFilter(filterName, FilterEnum.FILTER_REDUCEGREATEEQUAL, value);
	}

	/**
	 * 添加查询添加，比如 and (type = 1 or name = 2)，主要作用于拼接 and 后括号中的表达式，主要用于 or
	 * 查询的表达式，不然没必要。 从 V2.2.5.8 版本开始，该方法已被弃用，由方法 addOrParam 替代
	 * 
	 * @param param
	 */
	@Deprecated
	public void addParam(QueryProvider param) {
		if (param == null) {
			throw new HandleException("add param Error:param can't null(empty)!!!");
		}

		if (this.orParams == null) {
			orParams = new ArrayList<QueryProvider>();
		}

		this.orParams.add(param);
	}

	/**
	 * 添加查询添加，比如 and (type = 1 or name = 2)，主要作用于拼接 and 后括号中的表达式，主要用于 or
	 * 查询的表达式，不然没必要。 如果 多条件拼接 or 查询(类似 where id = ? and name = ? or type = 1
	 * 的条件)，or 条件查询不能被当成第一个条件放入(type属性 orFilter 方法不能在第一个加入)，否则会被解析为 and 条件查询。 V
	 * 2.2.5.8 开始启用
	 * 
	 * @param param
	 */
	public void addOrParam(QueryProvider param) {
		if (param == null) {
			throw new HandleException("add param Error:param can't null(empty)!!!");
		}

		if (this.orParams == null) {
			orParams = new ArrayList<QueryProvider>();
		}

		this.orParams.add(param);
	}

	/**
	 * * 添加 left join 查询，会被拼接到left join 的连体SQL。 当使用这个属性时，必须设置 connectTableName
	 * 的连接表名。
	 * 
	 * @param fieldName
	 *            表示left join 前面一张关联字段。
	 * @param paramFieldName
	 *            表示left join 后紧跟表的关联字段。
	 * @param param
	 *            被left join的封装对象。
	 */
	public void addLeftJoinParam(String fieldName, String paramFieldName, QueryProvider param) {
		if (fieldName == null) {
			throw new HandleException("add left join Error: left join fieldName can't null(empty)!!!");
		}
		if (paramFieldName == null) {
			throw new HandleException("add left join Error: left join paramFieldName can't null(empty)!!!");
		}
		if (param == null) {
			throw new HandleException("add left join param Error:param can't null(empty)!!!");
		}
		if (ValidateTool.isEmpty(param.getConnectTableName())) {
			throw new HandleException("add left join Error: param connectTableName can't null(empty)!!!");
		}

		if (this.leftJoinParams == null) {
			leftJoinParams = new ArrayList<Object[]>();
		}

		Object[] obj = { fieldName, paramFieldName, param };
		this.leftJoinParams.add(obj);
	}

	/**
	 * 添加 left join 查询，会被拼接到left join 的连体SQL。 当使用这个属性时，必须设置 connectTableName
	 * 的连接表名。 针对多条件，两数组长度必须一致。
	 * 
	 * @param fieldName
	 *            表示left join 前面一张关联字段。
	 * @param paramFieldName
	 *            表示left join 后紧跟表的关联字段。
	 * @param param
	 *            被left join的封装对象。
	 */
	public void addLeftJoinParam(String[] fieldName, String[] paramFieldName, QueryProvider param) {
		int fieldLength = 0;
		if (fieldName == null || (fieldLength = fieldName.length) == 0) {
			throw new HandleException("add left join Error: left join fieldName can't null(empty)!!!");
		}
		int paramFieldLength = 0;
		if (paramFieldName == null || (paramFieldLength = paramFieldName.length) == 0) {
			throw new HandleException("add left join Error: left join paramFieldName can't null(empty)!!!");
		}
		if (fieldLength != paramFieldLength) {
			throw new HandleException("add left join Error: left join 'on' filter length must be equal!!!");
		}
		if (param == null) {
			throw new HandleException("add param Error:param can't null(empty)!!!");
		}

		if (this.leftJoinParams == null) {
			leftJoinParams = new ArrayList<Object[]>();
		}

		Object[] obj = { fieldName, paramFieldName, param };
		this.leftJoinParams.add(obj);
	}

	/**
	 * 增加排序，参数分别为排序字段，排序值，排序值类型参考QueryParam中ORDER开头的常量
	 * 
	 * @param orderName
	 * @param orderType
	 */
	public void addOrder(String orderName, OrderEnum orderType) {
		if (ValidateTool.isEmpty(orderName)) {
			throw new HandleException("add order Error:order field can't null(empty)!!!");
		}

		if (this.orders == null) {
			this.orders = new ArrayList<String[]>();
		}
		abstractOrder.addOrder(orders, orderName, orderType);
		// this.orders.add("`" + orderName + "` " +
		// (OrderEnum.ORDER_ASC.equals(orderType) ? "asc" : "desc"));
	}

	/**
	 * 增加分组，根据字段名称进行分组
	 * 
	 * @param groupName
	 */
	public void addGroup(String groupName) {
		if (ValidateTool.isEmpty(groupName)) {
			throw new HandleException("add group Error: group field can't null(empty)!!!");
		}
		if (this.groups == null) {
			this.groups = new ArrayList<String>();
		}
		this.groups.add(groupName);
	}

	/**
	 * 移除所有查询条件
	 * @param
	 */
	public void removeFilter() {
		this.filters = null;
	}

	/**
	 * 根据前端传入的 command 实体，获取查询属性的 @QueryFilter 注解值
	 * @param obj
	 */
	public void setFilters(Object obj) {
		if (!(obj instanceof RequestParam)) {
			throw new HandleException("error: the filter commmand is not instanceof RequestQueryParam!!!");
		}
		QueryConvert.getFilters(obj, this);
	}

	/**
	 * 根据前端传入的 command 实体，获取修改属性的 @UpdateField 注解值
	 * @param obj
	 */
	public void setUpdateField(Object obj) {
		if (!(obj instanceof RequestParam)) {
			throw new HandleException("error: the update field obj is not instanceof RequestQueryParam!!!");
		}
		QueryConvert.getUpdateField(obj, this);
	}
	
	/**
	 * 传入 ResultOutput 的子类进行自动转换。
	 * 如果接收的属性与数据库字段不一致，用@Column注解映射，映射可以是实体属性名和字段名。
	 * 如果有属性不想被添加到addField中，用@NotMapper注解映射，将会自动过滤。
	 * @param cls
	 */
	public void setFieldColumn(Class<?> cls) {
		if(!ResultInfoOutput.class.isAssignableFrom(cls)) {
			throw new HandleException("error: the select field obj is not instanceof ResultOutput!!!");
		}
		
		List<String[]> result = BeanConvert.getResultFields(cls);
		for (String[] field : result) {
			this.addField(field[0], field[1]);
		}
	}

	/**
	 * 获取 ColumnParam 属性值
	 * @return
	 */
	private AbstractColumnParam getColumnParam() {
		if (columnParam == null) {
			initColumnParam();
		}
		return columnParam;
	}
	
	/**
	 * 初始化 ColumnParam 属性值
	 */
	private static synchronized void initColumnParam() {
		if (columnParam == null) {
			columnParam = new IntParam();
		}
	}
}
