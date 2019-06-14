package com.obatis.core.mapper.factory;

import com.obatis.core.exception.HandleException;
import com.obatis.core.mapper.BaseBeanSessionMapper;
import com.obatis.core.constant.CacheInfoConstant;
import org.apache.ibatis.session.SqlSession;

public class BeanSessionMapperFactory {
	
	private BeanSessionMapperFactory() {}

	public static BaseBeanSessionMapper<?> getSessionMapper(String canonicalName) throws HandleException {
		
		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return CacheInfoConstant.BEAN_SESSION_MAPPER.get(canonicalName);
		}

		throw new HandleException("error: sessionMapper is null");
	}


	
	public static synchronized void compileMapper(SqlSession sqlSession, String canonicalName) {
		
		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return;
		}
		compileMapperHandle(sqlSession, canonicalName);
	}

	private static synchronized void compileMapperHandle(SqlSession sqlSession, String canonicalName) {

		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return;
		}

		Class<?> mapperCls = null;
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
