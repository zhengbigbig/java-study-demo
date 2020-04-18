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
    
### 2.2 Spring常用注解

#### 2.2.1 @RequestBody与@ResponseBody
- @RequestBody修饰请求参数，注解用于接收HTTP的body，默认是使用JSON的格式
- @ResponseBody修饰返回值，注解用于在HTTP的body中携带响应数据，默认是使用JSON的格式，是一个数据接口。
如果不加该注解，spring响应字符串类型，是跳转到模板页面或jsp页面的开发模式，一个页面跳转控制器
#### 2.2.2 @RequestMapping注解
PostMapping等同于@RequestMapping的method等于POST。
同理：@GetMapping、@PutMapping、@DeleteMapping也都是简写的方式。
- value： 应用请求端点，最核心的属性，用于标志请求处理方法的唯一性；
- method： HTTP协议的method类型， 如：GET、POST、PUT、DELETE等；
- consumes： HTTP协议请求内容的数据类型（Content-Type），例如application/json, text/html;
- produces: HTTP协议响应内容的数据类型
- params： HTTP请求中必须包含某些参数值的时候，才允许被注解标注的方法处理请求。
- headers： HTTP请求中必须包含某些指定的header值，才允许被注解标注的方法处理请求。
#### 2.2.3 @RestController与@Controller
- @Controller注解，它的作用有两层含义：
    - 一是告诉Spring，被该注解标注的类是一个Spring的Bean，需要被注入到Spring的上下文环境中
    - 二是该类里面所有被RequestMapping标注的注解都是HTTP服务端点。
- @RestController相当于 @Controller和@ResponseBody结合。它有两层含义：
    - 一是作为Controller的作用，将控制器类注入到Spring上下文环境，该类RequestMapping标注方法为HTTP服务端点。
    - 二是作为ResponseBody的作用，请求响应默认使用的序列化方式是JSON，而不是跳转到jsp或模板页面。
#### 2.2.4 @PathVariable 与@RequestParam
- PathVariable用于URI上的{参数}
- RequestParam用于接收普通表单方式或者ajax模拟表单提交的参数数据。
```java
@DeleteMapping("/article/{id}")
public @ResponseBody AjaxResponse deleteArticle(@PathVariable Long id) {

@PostMapping("/article")
public @ResponseBody AjaxResponse deleteArticle(@RequestParam Long id) {
```
#### 2.2.5 @RequestBody 相比 @RequestParam
```java
import lombok.Data;
@Data
public class ParamData {
    private String name;
    private int id;
    private String phone;
    private BestFriend bestFriend;
    
    public static class BestFriend {
        private String address;
        private String sex;
    }
}
```
- 能够使用对象或者嵌套对象接收前端数据
- 使用getter/setter java plain model元素
- 上面成员变量名称一定要和JSON属性名称对应上
- 接受不同类型的参数，使用不同的成员变量类型

### 2.3 Http数据转换的原理
- 当一个HTTP请求到达时是一个InputStream，通过HttpMessageConverter转换为java对象，从而进行参数接收
- 当对一个HTTP请求进行响应时，我们首先输出的是一个java对象，然后由HttpMessageConverter转换为OutputStream输出
- 一般情况，是不需要自定义HttpMessageConverter，若需要自定义
    - 实现AbstractHttpMessageConverter接口
    - 指定该转换器是针对哪种数据格式的？如上文代码中的"application/vnd.ms-excel"
    - 指定该转换器针对那些对象数据类型？如上文代码中的supports函数
    - 使用writeInternal对数据进行输出处理，上例中是输出为Excel格式。
```java
@Service
public class TeamToXlsConverter extends AbstractHttpMessageConverter<Team> {

    private static final MediaType EXCEL_TYPE = MediaType.valueOf("application/vnd.ms-excel");

    TeamToXlsConverter() {
        super(EXCEL_TYPE);
    }

    @Override
    protected Team readInternal(final Class<? extends Team> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return (Team.class == clazz);
    }

    @Override
    protected void writeInternal(final Team team, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try (final Workbook workbook = new HSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet();
            int rowNo = 0;
            for (final TeamMember member : team.getMembers()) {
                final Row row = sheet.createRow(rowNo++);
                row.createCell(0)
                   .setCellValue(member.getName());
            }
            workbook.write(outputMessage.getBody());
        }
    }
}
```

# 3. JSON数据处理

## 3.1 Jackson 常用注解
- @JsonIgnore 排除属性不做序列化操作
- @JsonProperty
- @JsonPropertyOrder(value={"pname","pname2"})改变json子元素的顺序
- @JsonInclude(JsonInclude.Include.NON_NULL) 排除为空的元素不做序列化反序列化
- @JsonFormat(pattern = "yyyy-MM-dd HHmmss", timezone = "GMT+8") 指定属性格式

日期类型转化全局配置
```xml
spring:
    jackson:
        data-format: yyyy-MM-dd HH:mm:ss
        time-zone: GMT+8
```

## 3.2 