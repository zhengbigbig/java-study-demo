# RBAC权限管理模型

### 前置知识：

- h2
- mybatis
- flyway

### 说明： 
- 这里使用mybatis，简单起见，全部使用```@Mapper```，
当然你可以使用mybatis-plus或者jpa，随意
- 启动项目后，请确认h2数据库版本与 依赖一致
- 请修改配置文件.yml中的database url，使用绝对路径
- 连接数据库后，使用mvn flyway:migrate初始化数据库
- 若失败则先执行mvn flyway:clean之后再执行migrate
- 数据库表说明：
    - 用户表
    - 用户角色表、用户角色中间表
    - 访问资源表、角色资源中间表
- 一个用户可以拥有多个角色，一个角色可以拥有多个资源权限

## Role-Based Access Control

基于角色的权限控制

- 用户：系统接口及访问的操作者
- 权限：能够访问某接口或者做某操作的授权资格
- 角色：具有一类相同操作权限的用户的总称

### 1. 数据库动态加载用户权限

#### 1.1 流程

介绍：
- 前面的demo已经分析了认证的整个源码过程，因此我们需要自定义的东西并不多
- 基于数据库加载用户信息，spring security给我们提供了```DaoAuthenticationProvider```,
因此我们只需要实现```UserDetailsService```的```loadUserByUsername```来加载用户信息
- 从```UserDetailsService```加载出来的是```UserDetails```，
为了方便，我们使用自定义的User来继承该接口并实现，便于后续强转(强制类型转换)

##### 1.1.1 UserDetails实现
```java
public class User implements UserDetails
```
具体看源码
##### 1.1.2 UserDetailsService实现