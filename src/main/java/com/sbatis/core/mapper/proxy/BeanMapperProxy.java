package com.sbatis.core.mapper.proxy;

import com.sbatis.core.compile.JavaCompileFactory;

public class BeanMapperProxy {
	
	private BeanMapperProxy() {}

	public static Class<?> createMapper(String className) throws Exception {

		String packageName = className.substring(0, className.lastIndexOf(".")).replace(".entity", ".mapper");
		String entityName = className.substring(className.lastIndexOf(".") + 1);
		String name = entityName.replace("Entity", "Mapper");
		String javaSource = "package " + packageName + ";"
				+ "import com.pudahui.core.mapper.BaseBeanMapper;"
				+ "import " + className + ";"
				+ "public interface " + name + " extends BaseBeanMapper<" + entityName + "> "
				+ "{}";

		return JavaCompileFactory.compile(packageName, name, javaSource);
	}

}
