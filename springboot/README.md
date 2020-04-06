# Spring Boot Demo

## 1. branch boot-launch
### 1.1 Spring Boot Launch

## 常用插件和工具库
### 1.2 Lombok IDEA插件与之配合
列举部分：
- ```@Data```，自动写getter/setter/hashcode/equals/toString等
- 不需要写构造函数，只需要使用注解
    - 有参数的构造函数```@AllArgsConstructor```
    - 无参数的构造函数```@NoArgsConstructor```
- ```@Builder```开启builder模式
```xml
<!-- https://mvnrepository.com/artifact/org.projectlombok/lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.12</version>
    <scope>provided</scope>
</dependency>
```

### 1.3 热部署
#### 1.3.1 引入依赖
```xml
<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <version>2.2.6.RELEASE</version>
</dependency>
```
#### 1.3.2 配置插件
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <fork>true</fork>
    </configuration>
</plugin>
```
#### 1.3.3 配置IDEA自动构建
- settings -> compiler -> build project automatically
- command+option+shift+/ -> compiler.automake.allow.when.app.running 勾选上 

### 1.4 GsonFormat插件
- 根据JSON的结构来生成Java类(快捷键alt + s)
- 此外：谷歌插件可以安装JSONView

### 1.5 Maven Helper
- 分析依赖，解决冲突

## 2. branch restful
### 2.1 RESTFul风格 （基于http方法的API设计规范）
1. 看Url就知道要什么资源
2. 看http method就知道针对资源干什么
3. 看http status code就知道结果如何
4. Get方法和查询参数不应该改变数据
5. 使用复数名词 
    - /dogs 而不是 /dog
6. 复杂资源关系的表达
    - GET /cars/1/drivers/ 返回 使用car 1 的所有司机
    - GET /cars/1/drivers/1 返回 使用car 1 的1号司机
7. 高级用法::HATEOAS 超媒体应用状态引擎
    - 可以通过当前返回知道能做什么
8. 为集合提供过滤、排序、选择和分页等
    - /cars?color=red&seats<=2&current=1&pageSize=10
    - /cars?sort=-manufacturer,+model
    - /cars?fields=manufacturer,model,id,color
9. 版本化API
    - 使得API版本变得强制性，不要发布无版本的API。
    - /api/v1/blog 面向扩展开发，面向修改关闭。
    - 暴露出去的接口可能会有人使用，因此不能轻易地修改发布的已有接口