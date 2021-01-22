[Obatis开源框架项目主页：https://www.obatis.com/source/index.html](https://www.obatis.com/source/index.html)

Obatis开源框架是基于springboot、springcloud、mybatis等开源技术，旨在快速集成、统一开发模式和技术标准、真正实现Java面向对象的思想致力于为中小企业提供Java快速开发服务。项目于2019年4月开源至GitHub，其中包括工具包(Obatis-tools)、 HTTP网络通信(Obatis-net)、Office文件操作(Obatis-office，目前仅支持Excel的读和写操作)、服务端文件上传(Obatis-upload，目前基于FTP模式）、web应用开发(Obatis-web)、数据库编程（Obatis-core，其中已包含Obatis-web）等子项目，代码托管地址：https://github.com/obatis。

项目立项的原则是面向中小企业(团队)提供Java开发服务，减少项目繁琐配置，规范数据的输入和输出格式。丰富的API接口，基于Spring项目、源于ORM思想，真正体现Java面向对象的编程思维，为开发者或团队提供开箱即用的开源框架。项目理念为风格一致、减少重复工作、提高团队协作效率。


### 使用说明

Obatis开源框架于2020年08月26日发布的最新版本为 2.2.1-release，Obatis采用maven结构，引入Obatis只需在pom.xml中引入maven配置：

```
<dependency>
    <groupId>com.obatis</groupId>
    <artifactId>obatis-core</artifactId>
    <version>2.2.1-release</version>
</dependency>
```


项目启动类参考如下：


```
@StartupLoadAutoConfigure
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
@EnableScheduling
public class ApplicationStartup {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationStartup.class, args);
    }

}
```

MySql默认不支持批量更新，如需使用批量更新功能，需要开发人员主动数据库连接url后面显示配置加上&allowMultiQueries=true，例如：


```
spring.datasource.url = jdbc:mysql://127.0.0.1:3306/test_data?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true

```

### 参与项目

欢迎您参与Obatis项目的开发，如果有问题或者建议，欢迎直接提出Issue意见或者发送邮件至service@obatis.com，期待您的参与……
也可以加入QQ群：250577580，一起和其他人讨论、分享使用心得。更多内容尽在Obatis 。




### 关于捐助
如果你感觉Obatis项目对您有帮助，欢迎给我打赏 ———— 以激励我们快速完善新特性，感谢您对我们的肯定。
![avatar](https://www.obatis.com/static/img/accept_money/weixin_accept_money.png)

