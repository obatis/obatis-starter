package com.sbatis.core.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface Column {

	/**
	 * 字段名称
	 * @return
	 */
	String name();
}
