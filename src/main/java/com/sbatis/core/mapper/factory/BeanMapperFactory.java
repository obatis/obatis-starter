package com.sbatis.core.mapper.factory;

import com.sbatis.core.exception.HandleException;
import com.sbatis.core.mapper.BaseBeanMapper;
import com.sbatis.core.mapper.MapperCacheInfo;
import com.sbatis.core.mapper.proxy.BeanMapperProxy;
import org.apache.ibatis.session.SqlSession;

public class BeanMapperFactory {
	
	private BeanMapperFactory() {}

	public static BaseBeanMapper<?> getMapper(SqlSession sqlSession, String canonicalName) {
		
		if(MapperCacheInfo.BEAN_MAPPER.containsKey(canonicalName)) {
			return MapperCacheInfo.BEAN_MAPPER.get(canonicalName);
		}
		
		return compileMapper(sqlSession, canonicalName);
	}
	
	private static synchronized BaseBeanMapper<?> compileMapper(SqlSession sqlSession, String canonicalName) {
		
		if(MapperCacheInfo.BEAN_MAPPER.containsKey(canonicalName)) {
			return MapperCacheInfo.BEAN_MAPPER.get(canonicalName);
		}
		
		Class<?> mapperCls = null;
		try {
			mapperCls = BeanMapperProxy.createMapper(canonicalName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandleException("error: getMapper() is error !!!");
		}
		
		if(mapperCls == null) {
			throw new HandleException("error: getMapper() is error !!!");
		}

		sqlSession.getConfiguration().addMapper(mapperCls);
		BaseBeanMapper<?> mapper = (BaseBeanMapper<?>) sqlSession.getConfiguration().getMapper(mapperCls, sqlSession);
		if(mapper == null) {
			throw new HandleException("error: compileMapper() is fail !!!");
		}
		MapperCacheInfo.BEAN_MAPPER.put(canonicalName, mapper);
		return mapper; 
	}
}
