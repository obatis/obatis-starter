package com.obatis.core.mapper.factory;

import com.obatis.core.constant.CacheInfoConstant;
import com.obatis.core.exception.HandleException;
import com.obatis.core.mapper.BaseResultSessionMapper;
import org.apache.ibatis.session.SqlSession;

public class ResultSessionMapperFactory {
	
	private ResultSessionMapperFactory() {}

	public static BaseResultSessionMapper<?> getSessionMapper(String canonicalName) {
		
		if(CacheInfoConstant.RESULT_SESSION_MAPPER.containsKey(canonicalName)) {
			return CacheInfoConstant.RESULT_SESSION_MAPPER.get(canonicalName);
		}

		throw new HandleException("error: result sessionMapper is null");
	}
	
	public static synchronized void compileMapper(SqlSession sqlSession, String canonicalName) {
		
		if(CacheInfoConstant.RESULT_SESSION_MAPPER.containsKey(canonicalName)) {
			return;
		}

		compileMapperHandle(sqlSession, canonicalName);
	}

	private static synchronized void compileMapperHandle(SqlSession sqlSession, String canonicalName) {

		if(CacheInfoConstant.RESULT_SESSION_MAPPER.containsKey(canonicalName)) {
			return;
		}

		Class<?> mapperCls = null;
		try {
			mapperCls = SessionMapperCompilerTemplet.compilerMapper(canonicalName, BaseResultSessionMapper.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandleException("error: compilerMapper is fail");
		}

		if(mapperCls == null) {
			throw new HandleException("error: compilerMapper is fail");
		}

		sqlSession.getConfiguration().addMapper(mapperCls);
		BaseResultSessionMapper<?> resultMapper = (BaseResultSessionMapper<?>) sqlSession.getConfiguration().getMapper(mapperCls, sqlSession);
		if(resultMapper == null) {
			throw new HandleException("error: compilerMapper is fail");
		}
		CacheInfoConstant.RESULT_SESSION_MAPPER.put(canonicalName, resultMapper);
	}
}
