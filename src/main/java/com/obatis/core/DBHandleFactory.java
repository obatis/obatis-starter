package com.obatis.core;

import com.obatis.config.response.result.PageInfo;
import com.obatis.convert.BigDecimalConvert;
import com.obatis.convert.CommonConvert;
import com.obatis.core.constant.CacheInfoConstant;
import com.obatis.core.constant.SqlConstant;
import com.obatis.core.exception.HandleException;
import com.obatis.core.mapper.BaseBeanSessionMapper;
import com.obatis.core.mapper.BaseResultSessionMapper;
import com.obatis.core.mapper.factory.BeanSessionMapperFactory;
import com.obatis.core.mapper.factory.ResultSessionMapperFactory;
import com.obatis.core.result.ResultInfoOutput;
import com.obatis.core.sql.QueryProvider;
import com.obatis.core.sql.SqlHandleProvider;
import com.obatis.tools.ValidateTool;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DBHandleFactory 数据库操作类，提供对数据库操作的入口，并进行简要封装
 * @author HuangLongPu
 * @param <T>
 */
public abstract class DBHandleFactory<T extends CommonModel> {

	private Class<T> entityCls;
	private String tableName;
	private String canonicalName;
	private BaseBeanSessionMapper<T> baseBeanSessionMapper;


	/**
	 * 获取泛型注入类的 sessionMapper
	 * 如果未获取到，则进行编译处理操作，默认先从缓存中取值
	 * @return
	 */
	private BaseBeanSessionMapper<T> getBaseBeanSessionMapper() {

		if (entityCls == null) {
			getEntityCls();
		}
		if (baseBeanSessionMapper != null) {
			return baseBeanSessionMapper;
		}
		baseBeanSessionMapper = (BaseBeanSessionMapper<T>) BeanSessionMapperFactory.getSessionMapper(canonicalName);
		return baseBeanSessionMapper;
	}

	/**
	 * 获取 ResultInfoOutput 子类的 sessionMapper
	 * 需传入泛型class，如果未获取到，则进行编译处理操作，默认先从缓存中取值
	 * @param resultCls   结果集 class 类型
	 * @param <M>         泛型数据类型
	 * @return
	 * @throws HandleException
	 */
	private <M extends ResultInfoOutput> BaseResultSessionMapper<M> getBaseResultSessionMapper(Class<M> resultCls) throws HandleException {
		if (resultCls == null) {
			throw new HandleException("error: resultCls is null");
		}

		Map<String, BaseResultSessionMapper<M>> resultMapperMap = new HashMap<>();
		if (resultMapperMap.containsKey(resultCls.getCanonicalName())) {
			return resultMapperMap.get(resultCls.getCanonicalName());
		}

		BaseResultSessionMapper<M> resultMapper = (BaseResultSessionMapper<M>) ResultSessionMapperFactory.getSessionMapper(resultCls.getCanonicalName());
		resultMapperMap.put(resultCls.getCanonicalName(), resultMapper);
		return resultMapper;
	}

	/**
	 * 获取泛型注入的实体类
	 */
	private void getEntityCls() {
		entityCls = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		canonicalName = entityCls.getCanonicalName();
	}

	/**
	 * 获取存入缓存中的表名
	 * @return
	 * @throws HandleException
	 */
	public String getTableName() throws HandleException {

		if (entityCls == null) {
			getEntityCls();
		}
		if (ValidateTool.isEmpty(tableName)) {
			String clsName = entityCls.getCanonicalName();
			if (CacheInfoConstant.TABLE_CACHE.containsKey(clsName)) {
				tableName = CacheInfoConstant.TABLE_CACHE.get(clsName);
			}
		}
		return tableName;
	}

	/**
	 * 单个添加，传入一个 CommonEntity对象，并返回影响行数
	 * @param t    单个添加的实体数据
	 * @return
	 */
	public int insert(T t) throws HandleException {
		if (!(t instanceof CommonModel)) {
			throw new HandleException("error: entity is not instanceof CommonModel");
		}
		return this.getBaseBeanSessionMapper().insert(t, getTableName(), entityCls);
	}

	/**
	 * 批量添加，传入list CommonModel 对象，返回影响行数
	 * @param list
	 * @return
	 */
	public int batchInsert(List<T> list) throws HandleException {
		return this.getBaseBeanSessionMapper().insertBatch(list, getTableName(), entityCls);
	}

	/**
	 * 传入数据库封装操作对象 QueryProvider，进行更新
	 * @param provider
	 * @return
	 */
	public int update(QueryProvider provider) throws HandleException {
		
		if(provider == null) {
			throw new HandleException("error: update QueryProvider is null");
		}
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().update(paramMap, this.getTableName());
	}

	/**
	 * 批量更新，传入list 操作对象，返回影响行数
	 * @param list
	 * @return
	 * @throws HandleException
	 */
	public int batchUpdate(List<QueryProvider> list) throws HandleException {
		
		if(list == null || list.isEmpty()) {
			throw new HandleException("error: batchUpdate QueryProvider is empty");
		}
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, list);
		return this.getBaseBeanSessionMapper().updateBatch(paramMap, this.getTableName());
	}
	
	/**
	 * 根据传入的id主键，删除一条记录
	 * @param id
	 * @return
	 */
	public int deleteById(BigInteger id) throws HandleException {
		return this.getBaseBeanSessionMapper().deleteById(id, this.getTableName());
	}

	/**
	 * 根据传入的 QueryProvider 对象，进行删除操作
	 * @param provider
	 * @return
	 */
	public int delete(QueryProvider provider) throws HandleException {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().delete(paramMap, this.getTableName());
	}

	/**
	 * 1、根据id主键查询一条记录，返回所有字段。
	 * 2、如果根据条件有多条数据符合，则抛出异常。
	 * @param id
	 * @return
	 */
	public T findById(BigInteger id) {
		QueryProvider param = new QueryProvider();
		param.equals(CommonField.FIELD_ID, id);
		return this.find(param);
	}

	/**
	 * 1、根据id主键查询一条记录，返回所有字段，返回类型为预设的class类型，需强制转换一次。
	 * 2、如果根据条件有多条数据符合，则抛出异常。
	 * @param id
	 * @param resultCls
	 * @return
	 */
	public <M extends ResultInfoOutput> M findById(BigInteger id, Class<M> resultCls) {
		QueryProvider param = new QueryProvider();
		param.equals(CommonField.FIELD_ID, id);
		return this.find(param, resultCls);
	}

	/**
	 * 1、根据id主键查询一条记录，返回设定的字段。
	 * 2、如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @param id
	 * @return
	 */
	public T findById(QueryProvider provider, BigInteger id) {
		provider.equals(CommonField.FIELD_ID, id);
		return this.find(provider);
	}

	/**
	 * 1、根据id主键查询一条记录，返回设定的字段，返回类型为预设的class类型，需强制转换一次。
	 * 2、如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @param id
	 * @param resultCls
	 * @return
	 */
	public <M extends ResultInfoOutput> M findById(QueryProvider provider, BigInteger id, Class<M> resultCls) {
		provider.equals(CommonField.FIELD_ID, id);
		return this.find(provider, resultCls);
	}

	/**
	 * 1、根据传入的 QueryProvider 对象，查询一条 CommonModel 子类的记录。
	 * 2、如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public T find(QueryProvider provider) {
		Map<String, Object> providerMap = new HashMap<>();
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().find(providerMap, this.getTableName());
	}

	/**
	 * 1、根据传入的 QueryProvider 对象，返回类型为预设的class类型，需强制转换一次。
	 * 2、如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @param resultCls
	 * @return
	 */
	public <M extends ResultInfoOutput> M find(QueryProvider provider, Class<M> resultCls) {
		Map<String, Object> providerMap = new HashMap<>();
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseResultSessionMapper(resultCls).find(providerMap, this.getTableName());
	}

	/**
	 * 主要针对有多条记录符合查询条件时，获取第一条数据(排序方式自行决定)
	 * @param provider
	 * @return
	 */
	public T findOne(QueryProvider provider) {
		Map<String, Object> providerMap = new HashMap<>();
		provider.setLimit(1);
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().find(providerMap, this.getTableName());
	}

	/**
	 * 主要针对有多条记录符合查询条件时，获取第一条数据(排序方式自行决定)
	 * @param provider
	 * @param resultCls
	 * @param <M>
	 * @return
	 */
	public <M extends ResultInfoOutput> M findOne(QueryProvider provider, Class<M> resultCls) {
		Map<String, Object> providerMap = new HashMap<>();
		provider.setLimit(1);
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseResultSessionMapper(resultCls).find(providerMap, this.getTableName());
	}
	
	/**
	 * 1、主要作用为校验，provider 只需传入条件值即可，映射的SQL语句例如：select count(1) from test t where t.name='test';
	 * 2、根据 count 函数的返回值进行判断，返回值大于0表示存在，否则不存在。
	 * @param provider
	 * @return
	 */
	public boolean validate(QueryProvider provider) {
		Map<String, Object> providerMap = new HashMap<>();
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().validate(providerMap, this.getTableName()) > 0;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回一条Map格式记录。 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public Map<String, Object> findConvertMap(QueryProvider provider) {
		Map<String, Object> providerMap = new HashMap<>();
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().findToMap(providerMap, this.getTableName());
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回符合条件的list集合的BaseEntity记录。
	 * 如果有传入分页标识，只返回设置页面的极限值，否则返回所有符合条件的数据。
	 * @param provider
	 * @return
	 */
	public List<T> list(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().list(paramMap, this.getTableName());
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回符合条件的list集合，返回类型为预设的class类型，需强制转换一次。
	 * 如果有传入分页标识，只返回设置页面的极限值，否则返回所有符合条件的数据。
	 * @param provider
	 * @return
	 */
	public <M extends ResultInfoOutput> List<M> list(QueryProvider provider, Class<M> resultCls) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseResultSessionMapper(resultCls).list(paramMap, this.getTableName());
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回符合条件的List集合的Map格式记录。
	 * 如果有传入分页标识，只返回设置页面的极限值，否则返回所有符合条件的数据。
	 * @param provider
	 * @return
	 */
	public List<Map<String, Object>> listConvertMap(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().query(paramMap, this.getTableName());
	}

	/**
	 * 查询单个字段返回 List<Integer> 数据
	 * @param provider
	 * @return
	 */
	public List<Integer> listInteger(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().listInteger(paramMap, this.getTableName());
	}

	/**
	 * 查询单个字段返回 List<BigInteger> 数据
	 * @param provider
	 * @return
	 */
	public List<BigInteger> listBigInteger(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().listBigInteger(paramMap, this.getTableName());
	}

	/**
	 * 查询单个字段返回 List<Long> 数据
	 * @param provider
	 * @return
	 */
	public List<Long> listLong(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().listLong(paramMap, this.getTableName());
	}

	/**
	 * 查询单个字段返回 List<Double> 数据
	 * @param provider
	 * @return
	 */
	public List<Double> listDouble(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().listDouble(paramMap, this.getTableName());
	}

	/**
	 * 查询单个字段返回 List<BigDecimal> 数据
	 * @param provider
	 * @return
	 */
	public List<BigDecimal> listBigDecimal(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().listBigDecimal(paramMap, this.getTableName());
	}

	/**
	 * 查询单个字段返回 List<String> 数据
	 * @param provider
	 * @return
	 */
	public List<String> listString(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().listString(paramMap, this.getTableName());
	}

	/**
	 * 查询单个字段返回 List<Date> 数据
	 * @param provider
	 * @return
	 */
	public List<Date> listDate(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().listDate(paramMap, this.getTableName());
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回int的类型值。 该方法常用于查询count等类型的业务。
	 * 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public int findInt(QueryProvider provider) {
		Object obj = this.findObject(provider);
		if (obj != null) {
			if(obj instanceof Integer) {
				return (int) obj;
			} else if(obj instanceof Long) {
				return ((Long) obj).intValue();
			} else {
				return Integer.parseInt(CommonConvert.toString(obj));
			}
		}
		return 0;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回int的类型值。 该方法常用于查询count等类型的业务。
	 * 该方法与 findInt 用法区别在于根据查询条件会返回多条数据，取第一条，可根据使用场景进行排序。
	 * 如果能确保数据只有一条，建议使用 findInt 方法。
	 * @param provider
	 * @return
	 */
	public int findIntOne(QueryProvider provider) {
		provider.setLimit(1);
		List<Integer> list = this.listInteger(provider);
		if(list != null && list.isEmpty()) {
			return list.get(0);
		}
		return 0;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回 BigInteger 的类型值。 该方法常主要用于查询ID类型字段。
	 * @param provider
	 * @return
	 */
	public BigInteger findBigInteger(QueryProvider provider) {
		Object obj = this.findObject(provider);
		if(obj != null) {
			if(obj instanceof BigInteger) {
				return (BigInteger) obj;
			} else if (obj instanceof BigDecimal) {
				return ((BigDecimal) obj).toBigInteger();
			} else if (obj instanceof Double) {
				return BigInteger.valueOf(((Double) obj).longValue());
			} else if (obj instanceof Long) {
				return BigInteger.valueOf((Long) obj);
			} else if (obj instanceof Integer) {
				return BigInteger.valueOf((Integer) obj);
			} else {
				return new BigInteger(obj.toString());
			}
		}
		return null;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回 BigInteger 的类型值。 该方法常主要用于查询ID类型字段。
	 * 该方法与 findBigInteger 用法区别在于根据查询条件会返回多条数据，取第一条，可根据使用场景进行排序。
	 * 如果能确保数据只有一条，建议使用 findBigInteger 方法。
	 * @param provider
	 * @return
	 */
	public BigInteger findBigIntegerOne(QueryProvider provider) {
		provider.setLimit(1);
		List<BigInteger> list = this.listBigInteger(provider);
		if(list != null && list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回int的类型值。
	 * 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public Long findLong(QueryProvider provider) {
		Object obj = this.findObject(provider);
		if (obj != null) {
			if(obj instanceof Long) {
				return (Long) obj;
			} else if (obj instanceof BigDecimal) {
				return ((BigDecimal) obj).longValue();
			} else if (obj instanceof Double) {
				return ((Double) obj).longValue();
			} else if (obj instanceof Float) {
				return ((Float) obj).longValue();
			} else {
				return Long.parseLong(CommonConvert.toString(obj));
			}

		}
		return 0L;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回int的类型值。
	 * 该方法与 findLong 用法区别在于根据查询条件会返回多条数据，取第一条，可根据使用场景进行排序。
	 * 如果能确保数据只有一条，建议使用 findLong 方法。
	 * @param provider
	 * @return
	 */
	public Long findLongOne(QueryProvider provider) {
		provider.setLimit(1);
		List<Long> list = this.listLong(provider);
		if(list != null && list.isEmpty()) {
			return list.get(0);
		}
		return 0L;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回Double的类型值。
	 * 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public Double findDouble(QueryProvider provider) {
		Object obj = this.findObject(provider);
		if (obj != null) {
			if(obj instanceof Double) {
				return (Double) obj;
			} else if (obj instanceof BigDecimal) {
				return ((BigDecimal) obj).doubleValue();
			} else if (obj instanceof Float) {
				return ((Float) obj).doubleValue();
			} else if (obj instanceof Long) {
				return ((Long) obj).doubleValue();
			} else if (obj instanceof Integer) {
				return ((Integer) obj).doubleValue();
			}
		}
		return 0D;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回Double的类型值。
	 * 该方法与 findDouble 用法区别在于根据查询条件会返回多条数据，取第一条，可根据使用场景进行排序。
	 * 如果能确保数据只有一条，建议使用 findDouble 方法。
	 * @param provider
	 * @return
	 */
	public Double findDoubleOne(QueryProvider provider) {
		provider.setLimit(1);
		List<Double> list = this.listDouble(provider);
		if(list != null && list.isEmpty()) {
			return list.get(0);
		}
		return 0D;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回BigDecimal的类型值。 该方法常用于查询金额字段。
	 * 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public BigDecimal findBigDecimal(QueryProvider provider) {
		Object obj = this.findObject(provider);
		if (obj != null) {
			if(obj instanceof BigDecimal) {
				return (BigDecimal) obj;
			} else if (obj instanceof String) {
				return BigDecimalConvert.convert((String) obj);
			} else if (obj instanceof Integer) {
				return BigDecimalConvert.convert((Integer) obj);
			} else {
				return BigDecimalConvert.convert(obj.toString());
			}
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回BigDecimal的类型值。 该方法常用于查询金额字段。
	 * 该方法与 findBigDecimal 用法区别在于根据查询条件会返回多条数据，取第一条，可根据使用场景进行排序。
	 * 如果能确保数据只有一条，建议使用 findBigDecimal 方法。
	 * @param provider
	 * @return
	 */
	public BigDecimal findBigDecimalOne(QueryProvider provider) {
		provider.setLimit(1);
		List<BigDecimal> list = this.listBigDecimal(provider);
		if(list != null && list.isEmpty()) {
			return list.get(0);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回 Date 的类型值。
	 * 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public Date findDate(QueryProvider provider) {
		Object obj = this.findObject(provider);
		if(obj != null && obj instanceof Date) {
			return (Date) obj;
		}
		return null;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回 Date 的类型值。
	 * 该方法与 findDate 用法区别在于根据查询条件会返回多条数据，取第一条，可根据使用场景进行排序。
	 * 如果能确保数据只有一条，建议使用 findDate 方法。
	 * @param provider
	 * @return
	 */
	public Date findDateOne(QueryProvider provider) {
		provider.setLimit(1);
		List<Date> list = this.listDate(provider);
		if(list != null && list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回 String 的类型值。
	 * 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	public String findString(QueryProvider provider) {
		Object obj = this.findObject(provider);
		if(obj != null) {
			return CommonConvert.toString(obj);
		}
		return null;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回 String 的类型值。
	 * 该方法与 findString 用法区别在于根据查询条件会返回多条数据，取第一条，可根据使用场景进行排序。
	 * 如果能确保数据只有一条，建议使用 findString 方法。
	 * @param provider
	 * @return
	 */
	public String findStringOne(QueryProvider provider) {
		provider.setLimit(1);
		List<String> list = this.listString(provider);
		if(list != null && list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 根据传入的 QueryProvider 对象，返回Object的类型值。
	 * 如果根据条件有多条数据符合，则抛出异常。
	 * @param provider
	 * @return
	 */
	private Object findObject(QueryProvider provider) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		return this.getBaseBeanSessionMapper().findObject(paramMap, this.getTableName());
	}

	/**
	 * 需传入的条件值。
	 * @param sql       sql语句中的条件，用 "?" 号代替，防止SQL注入
	 * @param params    需传入的条件值，按顺序存放
	 * @return
	 */
	public T findBySql(String sql, List<Object> params) {
		return this.getBaseBeanSessionMapper().findBySql(sql, params);
	}

	/**
	 * 返回Object 类型，比如int、decimal、String等。
	 * @param sql
	 * @param params
	 * @return
	 */
	public Object findObjectBySql(String sql, List<Object> params) {
		return this.getBaseBeanSessionMapper().findObjectBySql(sql, params);
	}

	/**
	 * 获取总条数，针对count 等SQL语句。
	 * @param sql
	 * @param params
	 * @return
	 */
	public int findTotal(String sql, List<Object> params) {
		return this.getBaseBeanSessionMapper().findTotalByParam(sql, params);
	}

	/**
	 * 传入SQL，返回预设类型对象。返回类型为预设的class类型，需强制转换一次。
	 * @param sql          sql语句中的条件，用 "?" 号代替，防止SQL注入
	 * @param params       需传入的条件值，按顺序存放
	 * @param resultCls    返回类型
	 * @return
	 */
	public <M extends ResultInfoOutput> M findBySql(String sql, List<Object> params, Class<M> resultCls) {
		return this.getBaseResultSessionMapper(resultCls).findBySql(sql, params);
	}

	/**
	 * 传入SQL，返回map类型。
	 * @param sql    sql语句中的条件，用 "?" 号代替，防止SQL注入
	 * @param list   需传入的条件值，按顺序存放
	 * @return
	 */
	public Map<String, Object> findMapBySql(String sql, List<Object> list) {
		return this.getBaseBeanSessionMapper().findMapBySql(sql, list);
	}

	/**
	 * 根据传入的SQL语句，返回符合条件的list集合的Map格式记录。
	 * @param sql     sql语句中的条件，用 "?" 号代替，防止SQL注入
	 * @param params  需传入的条件值，按顺序存放
	 * @return
	 */
	public List<T> listBySql(String sql, List<Object> params) {
		return this.getBaseBeanSessionMapper().listBySql(sql, params);
	}

	/**
	 * 传入SQL，返回预设类型集合。返回类型为预设的class类型，需强制转换一次。
	 * @param sql          sql语句中的条件，用 "?" 号代替，防止SQL注入
	 * @param params       需传入的条件值，按顺序存放
	 * @param resultCls    返回bean类型
	 * @return
	 */
	public <M extends ResultInfoOutput> List<M> listBySql(String sql, List<Object> params, Class<M> resultCls) {
		return this.getBaseResultSessionMapper(resultCls).listBySql(sql, params);
	}

	/**
	 * 根据传入的SQL语句，返回符合条件的list集合的Map格式记录。
	 * @param sql      sql语句中的条件，用 "?" 号代替，防止SQL注入
	 * @param params   需传入的条件值，按顺序存放
	 * @return
	 */
	public List<Map<String, Object>> listMapBySql(String sql, List<Object> params) {
		return this.getBaseBeanSessionMapper().listMapBySql(sql, params);
	}

	/**
	 * 主要实现于在前端查询时选中的页面超过总条数，非前端分页查询，不建议使用。
	 * 分页查询，同时返回分页数据和总条数。
	 * @param sql           主体查询语句
	 * @param totalSql      总条数查询语句
	 * @param params        条件值
	 * @param pageNumber    页码
	 * @param pageSize      每行显示条数
	 * @return
	 */
	public PageInfo<T> page(String sql, String totalSql, List<Object> params, int pageNumber, int pageSize) {

		int total = this.findTotal(totalSql, params);
		PageInfo<T> page = new PageInfo<>();
		page.setTotal(total);
		if (total == 0) {
			// 当没有数据的时候，直接不进行数据查询
			return page;
		}
		sql = SqlHandleProvider.appendPageSql(sql, pageNumber, pageSize);
		page.setList(this.getBaseBeanSessionMapper().listBySql(sql, params));
		return page;
	}

	/**
	 * 主要实现于在前端查询时选中的页面超过总条数，非前端分页查询，不建议使用。
	 * 分页查询，同时返回分页数据和总条数。
	 * @param sql           主体查询语句
	 * @param totalSql      总条数查询语句
	 * @param params        条件值
	 * @param pageNumber    页码
	 * @param pageSize      每行显示条数
	 * @param resultCls     resultCls 返回 预定义的 resultCls Bean 泛型数据类型
	 * @return
	 */
	public <M extends ResultInfoOutput> PageInfo<M> page(String sql, String totalSql, List<Object> params, int pageNumber, int pageSize, Class<M> resultCls) {
		int total = this.findTotal(totalSql, params);
		PageInfo<M> page = new PageInfo<>();
		page.setTotal(total);
		if (total == 0) {
			// 当没有数据的时候，直接不进行数据查询
			return page;
		}
		sql = SqlHandleProvider.appendPageSql(sql, pageNumber, pageSize);
		page.setList(this.getBaseResultSessionMapper(resultCls).listBySql(sql, params));
		return page;
	}

	/**
	 * 主要实现于在前端查询时选中的页面超过总条数，非前端分页查询，不建议使用。
	 * 分页查询，同时返回分页数据和总条数，返回 Map 数据。
	 * @param sql           主体查询语句
	 * @param totalSql      总条数查询语句
	 * @param params        条件值
	 * @param pageNumber    页面
	 * @param pageSize      每行显示条数
	 * @return
	 */
	public PageInfo<Map<String, Object>> pageResultMap(String sql, String totalSql, List<Object> params, int pageNumber, int pageSize) {
		int total = this.findTotal(totalSql, params);
		PageInfo<Map<String, Object>> page = new PageInfo<>();
		page.setTotal(total);
		if (total == 0) {
			// 当没有数据的时候，直接不进行数据查询
			return page;
		}
		sql = SqlHandleProvider.appendPageSql(sql, pageNumber, pageSize);
		page.setList(this.getBaseBeanSessionMapper().listMapBySql(sql, params));
		return page;
	}

	/**
	 * 主要实现于在前端查询时选中的页面超过总条数，非前端分页查询，不建议使用。
	 * 分页查询，同时返回分页数据和总条数。
	 * @param provider 封装的参数对象
	 * @return
	 */
	public PageInfo<T> page(QueryProvider provider) {
		Map<String, Object> providerMap = new HashMap<>();
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		// 拼装SQL语句
		SqlHandleProvider.getQueryPageSql(providerMap, this.getTableName());

		int total = this.getBaseBeanSessionMapper().findTotal((String) providerMap.get(SqlConstant.PROVIDER_COUNT_SQL), providerMap);
		PageInfo<T> page = new PageInfo<>();
		page.setTotal(total);
		if (total == 0) {
			// 当总条数为0时，直接取消数据查询
			return page;
		}

		String querySql = (String) providerMap.get(SqlConstant.PROVIDER_QUERY_SQL);
		providerMap.put(SqlConstant.PROVIDER_OBJ, provider);
		page.setList(this.getBaseBeanSessionMapper().page(querySql, providerMap));
		return page;
	}
	
	/**
	 * 主要实现于在前端查询时选中的页面超过总条数，非前端分页查询，不建议使用。
	 * 分页查询，同时返回分页数据和总条数。
	 * @param provider       封装的参数对象
	 * @param resultCls      返回 预定义的 resultCls Bean 泛型数据类型
	 * @return
	 */
	public <M extends ResultInfoOutput> PageInfo<M> page(QueryProvider provider, Class<M> resultCls) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		// 拼装SQL语句
		SqlHandleProvider.getQueryPageSql(paramMap, this.getTableName());

		int total = this.getBaseBeanSessionMapper().findTotal((String) paramMap.get(SqlConstant.PROVIDER_COUNT_SQL), paramMap);
		PageInfo<M> page = new PageInfo<>();
		page.setTotal(total);
		
		if (total == 0) {
			// 当总条数为0时，直接取消数据查询
			return page;
		}

		String querySql = (String) paramMap.get(SqlConstant.PROVIDER_QUERY_SQL);
		paramMap.put(SqlConstant.PROVIDER_OBJ, provider);
		page.setList(this.getBaseResultSessionMapper(resultCls).page(querySql, paramMap));
		return page;
	}

//	private long getPages(long total, int pageSize) {
//		long pages = total / pageSize;
//		if (total % pageSize != 0) {
//			pages += 1;
//		}
//		return pages;
//	}

}
