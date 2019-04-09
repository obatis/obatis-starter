package com.sbatis.core.mapper.factory;

import com.sbatis.core.exception.HandleException;
import com.sbatis.core.mapper.BaseResultMapper;
import com.sbatis.core.mapper.MapperCacheInfo;
import com.sbatis.core.mapper.proxy.ResultMapperProxy;
import com.sbatis.core.result.ResultInfoOutput;
import org.apache.ibatis.session.SqlSession;

public class ResultMapperFactory {
	
	private ResultMapperFactory() {}

	public static BaseResultMapper<?> getMapper(SqlSession sqlSession, Class<?> resultCls) {
		
		String canonicalName = resultCls.getCanonicalName();
		if(MapperCacheInfo.RESULT_MAPPER.containsKey(canonicalName)) {
			return MapperCacheInfo.RESULT_MAPPER.get(canonicalName);
		}
		
		return compileMapper(resultCls, sqlSession, canonicalName);
	}
	
	private static synchronized BaseResultMapper<?> compileMapper(Class<?> resultCls, SqlSession sqlSession, String canonicalName) {
		
		if(MapperCacheInfo.RESULT_MAPPER.containsKey(canonicalName)) {
			return MapperCacheInfo.RESULT_MAPPER.get(canonicalName);
		}
		
		try {
			if(!(resultCls.newInstance()instanceof ResultInfoOutput)) {
				throw new HandleException("error: the Class<?> resultCls is not instanceof ResultOutput !!!");
			}
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		Class<?> mapperCls = null;
		try {
			mapperCls = ResultMapperProxy.createMapper(resultCls.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandleException("error: getResultMapper() is error !!!");
		}
		
		if(mapperCls == null) {
			throw new HandleException("error: getResultMapper() is error !!!");
		}
		
		sqlSession.getConfiguration().addMapper(mapperCls);
		BaseResultMapper<?> resultMapper = (BaseResultMapper<?>) sqlSession.getConfiguration().getMapper(mapperCls, sqlSession);
		if(resultMapper == null) {
			throw new HandleException("error: compileMapper() is fail !!!");
		}
		MapperCacheInfo.RESULT_MAPPER.put(canonicalName, resultMapper);
		return resultMapper; 
	}
}
