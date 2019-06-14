package com.obatis.startup;

import com.obatis.config.SystemConstant;
import com.obatis.core.annotation.Table;
import com.obatis.core.convert.BeanCacheConvert;
import com.obatis.core.mapper.factory.BeanSessionMapperFactory;
import com.obatis.core.mapper.factory.ResultSessionMapperFactory;
import com.obatis.core.result.ResultInfoOutput;
import org.apache.ibatis.session.SqlSession;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;
import java.util.Set;

@Configuration
@Order(-99999)
public class AnnotationBeanHandle {

	@Resource
	private SqlSession sqlSession;

	@Bean
	public int onApplicationEvent() {

		Reflections reflections = new Reflections(SystemConstant.PROJECT_BASE_DIR);
		/**
		 * 将注解的表加载到缓存
		 */
		Set<Class<?>> classList = reflections.getTypesAnnotatedWith(Table.class);
		for (Class<?> cls : classList) {
			BeanCacheConvert.loadEntityCache(cls);

			BeanSessionMapperFactory.compileMapper(sqlSession, cls.getCanonicalName());
		}

		Set<Class<? extends ResultInfoOutput>> resultOutputClass = reflections.getSubTypesOf(ResultInfoOutput.class);
		for (Class<? extends ResultInfoOutput> cls : resultOutputClass) {
			ResultSessionMapperFactory.compileMapper(sqlSession, cls.getCanonicalName());
		}

		return 0;
	}
}
