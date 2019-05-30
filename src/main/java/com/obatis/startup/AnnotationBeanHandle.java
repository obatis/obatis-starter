package com.obatis.startup;

import com.obatis.config.SystemConstant;
import com.obatis.core.annotation.Table;
import com.obatis.core.convert.BeanCacheConvert;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Set;

@Configuration
@Order(-99999)
//public class AnnotationBeanHandle implements ApplicationListener<ContextRefreshedEvent> {
public class AnnotationBeanHandle {

	@Bean
//	public int onApplicationEvent(ContextRefreshedEvent event) {
	public int onApplicationEvent() {

//		if(event.getApplicationContext() instanceof AnnotationConfigServletWebServerApplicationContext) {
			Reflections reflections = new Reflections(SystemConstant.PROJECT_BASE_DIR);
			/**
			 * 将注解的表加载到缓存
			 */
			Set<Class<?>> classList = reflections.getTypesAnnotatedWith(Table.class);
			for (Class<?> cls : classList) {
				BeanCacheConvert.loadEntityCache(cls);
			}
//		}
		return 0;
	}
}
