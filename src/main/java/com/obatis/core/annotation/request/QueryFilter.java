package com.obatis.core.annotation.request;

import com.obatis.core.constant.type.DateHandleEnum;
import com.obatis.core.constant.type.FilterEnum;

import java.lang.annotation.*;

/**
 * 类型注解，默认name为属性名，查询条件为等于，属性为空过滤
 * @author HuangLongPu
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryFilter {

	/**
	 * 映射的数据库字段名或者entity的属性名，默认为对象属性名
	 * @return
	 */
	String name() default "";
	
	/**
	 * 表示查询条件，默认为等于，具体值参考QueryParam类中"FILTER_"开头的常量
	 * @return
	 */
	FilterEnum type() default FilterEnum.FILTER_EQUAL;
	
	/**
	 * 是否可以空，默认不能为空，如果属性为空加有注解直接过滤
	 * @return
	 */
	boolean isnull() default false;
	
	/**
	 * 表示时间区间
	 * @return
	 */
	DateHandleEnum datetype() default DateHandleEnum.NOT_HANDLE;
}
