# Spring Boot Demo

## Spring Boot
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