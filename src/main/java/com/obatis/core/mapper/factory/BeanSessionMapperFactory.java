package com.obatis.core.mapper.factory;

import com.obatis.core.exception.HandleException;
import com.obatis.core.mapper.BaseBeanSessionMapper;
import com.obatis.core.constant.CacheInfoConstant;
import org.apache.ibatis.session.SqlSession;

public class BeanSessionMapperFactory {
	
	private BeanSessionMapperFactory() {}

	public static BaseBeanSessionMapper<?> getSessionMapper(SqlSession sqlSession, String canonicalName) {
		
		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return CacheInfoConstant.BEAN_SESSION_MAPPER.get(canonicalName);
		}
		
		return compileMapper(sqlSession, canonicalName);
	}
	
	private static synchronized BaseBeanSessionMapper<?> compileMapper(SqlSession sqlSession, String canonicalName) {
		
		if(CacheInfoConstant.BEAN_SESSION_MAPPER.containsKey(canonicalName)) {
			return CacheInfoConstant.BEAN_SESSION_MAPPER.get(canonicalName);
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
		return mapper; 
	}
}
