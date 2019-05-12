package com.obatis.core.mapper.factory;

import com.obatis.core.compile.JavaCompilerFactory;

import java.net.URISyntaxException;

/**
 * sessionMapper 编译模版，构建sessionMappper接口类
 * @author HuangLongPu
 */
public class SessionMapperCompilerTemplet {

    private SessionMapperCompilerTemplet() {}

    /**
     * 构建 sessionMappper 接口类
     * @author HuangLongPu
     * @param canonicalName
     * @param mapperCls
     * @return
     * @throws URISyntaxException
     * @throws ClassNotFoundException
     */
    public static Class<?> compilerMapper(String canonicalName, Class<?> mapperCls) throws URISyntaxException, ClassNotFoundException {
        String packageName = canonicalName.substring(0, canonicalName.lastIndexOf("."));
        String entityName = canonicalName.substring(canonicalName.lastIndexOf(".") + 1);
        String javaName = entityName + "Mapper";
        String javaSource = "package " + packageName + ";"
                + "import " + canonicalName + ";"
                + "public interface " + javaName + " extends " + mapperCls.getCanonicalName() + "<" + entityName + "> "
                + "{}";

        return JavaCompilerFactory.compiler(packageName, javaName, javaSource);
    }

}
