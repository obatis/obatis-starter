# obatis-core
obatis 体系核心项目，支持数据库编码，基于springcloud + mybatis 数据库编程。项目主页:https://www.obatis.com/source/index.html, GitHub代码托管地址：https://github.com/obatis/obatis-core

maven jar包引入在maven中央仓库搜索obatis，引用相应的版本，或者打开链接 https://mvnrepository.com/artifact/com.obatis/obatis-core 找到相应的版本，引入项目即可。

针对MySQL 数据库连接：

- springboot 2.2.x及以上默认连接的是mysql 8.x的数据，如果要连接到5.x的数据库，需要显式配置

  ```xml
  spring.datasource.driver-class-name=com.mysql.jdbc.Driver
  ```

