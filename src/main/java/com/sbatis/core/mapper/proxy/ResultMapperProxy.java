package com.sbatis.core.mapper.proxy;

import com.sbatis.core.compile.JavaCompileFactory;

public class ResultMapperProxy {
	
	private ResultMapperProxy(){}

	public static Class<?> createMapper(String className) throws Exception {

		String packageName = className.substring(0, className.lastIndexOf("."));
		
		String entityName = className.substring(className.lastIndexOf(".") + 1);
		String name = entityName + "Mapper";
		String javaSource = "package " + packageName + ";"
				+ "import com.pudahui.core.mapper.BaseResultMapper;"
				+ "import " + className + ";"
				+ "public interface " + name + " extends BaseResultMapper<" + entityName + "> "
				+ "{}";

		return JavaCompileFactory.compile(packageName, name, javaSource);
	}

}
