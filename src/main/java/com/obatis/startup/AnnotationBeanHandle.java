package com.obatis.startup;

import com.obatis.core.annotation.Table;
import com.obatis.core.convert.BeanCacheConvert;
import org.reflections.Reflections;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(0)
public class AnnotationBeanHandle implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if(event.getApplicationContext() instanceof AnnotationConfigServletWebServerApplicationContext) {
			Reflections reflections = new Reflections(SystemConstant.PROJECT_BASE_DIR);
			/**
			 * 将注解的表加载到缓存
			 */
			Set<Class<?>> classList = reflections.getTypesAnnotatedWith(Table.class);
			for (Class<?> cls : classList) {
				BeanCacheConvert.loadEntityCache(cls);
			}
		}
	}
}
