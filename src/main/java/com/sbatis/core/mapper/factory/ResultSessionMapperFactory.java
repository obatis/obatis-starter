package com.sbatis.core.mapper.factory;

import com.sbatis.core.exception.HandleException;
import com.sbatis.core.mapper.BaseResultSessionMapper;
import com.sbatis.core.constant.CacheInfoConstant;
import org.apache.ibatis.session.SqlSession;

public class ResultSessionMapperFactory {
	
	private ResultSessionMapperFactory() {}

	public static BaseResultSessionMapper<?> getSessionMapper(SqlSession sqlSession, String canonicalName) {
		
		if(CacheInfoConstant.RESULT_SESSION_MAPPER.containsKey(canonicalName)) {
			return CacheInfoConstant.RESULT_SESSION_MAPPER.get(canonicalName);
		}
		
		return compileMapper(sqlSession, canonicalName);
	}
	
	private static synchronized BaseResultSessionMapper<?> compileMapper(SqlSession sqlSession, String canonicalName) {
		
		if(CacheInfoConstant.RESULT_SESSION_MAPPER.containsKey(canonicalName)) {
			return CacheInfoConstant.RESULT_SESSION_MAPPER.get(canonicalName);
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
		return resultMapper; 
	}
}
