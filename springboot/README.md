# Spring Boot 

### 1.1 Spring Boot Launch
常用插件和工具库
#### Lombok IDEA插件与之配合
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

