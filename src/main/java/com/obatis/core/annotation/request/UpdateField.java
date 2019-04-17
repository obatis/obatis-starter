package com.obatis.core.annotation.request;

import com.obatis.core.constant.type.SqlHandleEnum;

import java.lang.annotation.*;

/**
 * 类型注解，默认name为属性名，type默认为常规，属性为空过滤
 * @author HuangLongPu
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UpdateField {

	/**
	 * 映射的数据库字段名或者entity的属性名，默认为对象属性名
	 * @return
	 */
	String name() default "";
	
	/**
	 * 修改时内容，分别为常规默认，如：type = 1；累加，如：sum = sum + 1；累减，如：sum = sum - 1
	 * @return
	 */
	SqlHandleEnum type() default SqlHandleEnum.HANDLE_DEFAULT;
	
	/**
	 * 是否可以空，默认不能为空，如果属性为空加有注解直接过滤
	 * @return
	 */
	boolean isnull() default false;
}
