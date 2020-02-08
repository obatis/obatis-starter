# obatis-core
obatis 体系核心项目，支持数据库编码，基于springcloud + mybatis


maven jar包引入在maven中央仓库搜索obatis，引用相应的版本，或者打开链接 https://mvnrepository.com/artifact/com.obatis/obatis-core 即可。

springboot 2.2.x及以上默认连接的是mysql 8.x的数据，如果要连接到5.x的数据库，需要显式、】/-。、指定spring.datasource.driver-class-name=com.mysql.jdbc.Driver