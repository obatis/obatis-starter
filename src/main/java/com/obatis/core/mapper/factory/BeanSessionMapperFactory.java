package com.obatis.core.mapper.factory;

import com.obatis.core.exception.HandleException;
import com.obatis.core.mapper.BaseBeanSessionMapper;
import com.obatis.core.constant.CacheInfoConstant;
import org.apache.ibatis.session.SqlSession;

/**
 * 构建 bean mappper 类
 */
public class BeanSessionMapperFactory {
	
	private BeanSessionMapperFactory() {}

	/**
	 * 根据构建的bean，获取 mapper
	 * @param canonicalName
	 * @return
	 * @throws HandleException
	 */
	public static BaseBeanSessionMapper<?> getSessionMapper(String canonicalName) throws HandleException {

		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return CacheInfoConstant.BEAN_SESSION_MAPPER.get(canonicalName);
		}

		throw new HandleException("error: sessionMapper is null");
	}

	/**
	 * 动态构建 mapper 类
	 * @param sqlSession
	 * @param canonicalName
	 */
	public static synchronized void compileMapper(SqlSession sqlSession, String canonicalName) {

		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return;
		}
		compileMapperHandle(sqlSession, canonicalName);
	}

	/**
	 * 动态构建 mapper 类实现
	 * @param sqlSession
	 * @param canonicalName
	 */
	private static synchronized void compileMapperHandle(SqlSession sqlSession, String canonicalName) {

		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return;
		}

		Class<?> mapperCls;
		try {
			mapperCls = SessionMapperCompilerTemplet.compilerMapper(canonicalName, BaseBeanSessionMapper.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandleException("error: compilerMapper is fail");
		}

		if(mapperCls == null) {
			throw new HandleException("error: compilerMapper is fail");
		}

		sqlSession.getConfiguration().addMapper(mapperCls);
		BaseBeanSessionMapper<?> mapper = (BaseBeanSessionMapper<?>) sqlSession.getConfiguration().getMapper(mapperCls, sqlSession);
		if(mapper == null) {
			throw new HandleException("error: compilerMapper is fail");
		}
		CacheInfoConstant.BEAN_SESSION_MAPPER.put(canonicalName, mapper);
	}
}
