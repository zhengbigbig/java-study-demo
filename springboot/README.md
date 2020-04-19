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

# 4. Mockito编码完成接口测试

## 4.1 注解说明（以junit5）
- @Test 声明一个测试方法
- @BeforeAll 在当前类的所有测试方法之前执行。注解在【静态方法】上
- @AfterAll 在当前类中的所有测试方法之后执行。注解在【静态方法】上
- @BeforeEach 在每个测试方法之前执行。注解在【非静态方法】上
- @AfterEach 在每个测试方法之后执行。注解在【非静态方法】

- @SpringBootTest 
是用来创建Spring的上下文ApplicationContext，保证测试在上下文环境里运行。
单独使用@SpringBootTest不会启动servlet容器。
所以只是使用SpringBootTest 注解，不可以使用@Resource和@Autowired等注解进行bean的依赖注入。（准确的说是可以使用，但被注解的bean为null）。
- @Transactional
可以使单元测试进行事务回滚，以保证数据库表中没有因测试造成的垃圾数据，因此保证单元测试可以反复执行；
不建议这么做，使用该注解会破坏测试真实性。

## 4.2 MockMvc对象的几个基本方法
- perform : 执行一个RequestBuilder请求，会自动执行SpringMVC的流程并映射到相应的控制器Controller执行处理。
- contentType：发送请求内容的序列化的格式，"application/json"表示JSON数据格式
- andExpect: 添加RequsetMatcher验证规则，验证控制器执行完成后结果是否正确，或者说是结果是否与我们期望（Expect）的一致。
- andDo: 添加ResultHandler结果处理器，比如调试时打印结果到控制台
- andReturn: 最后返回相应的MvcResult,然后进行自定义验证/进行下一步的异步处理

## 4.3 @RunWith注解
- RunWith方法为我们构造了一个的Servlet容器运行运行环境，并在此环境下测试。
- 而@AutoConfigureMockMvc注解，该注解表示 MockMvc由spring容器构建，你只负责注入之后用就可以了。这种写法是为了让测试在Spring容器环境下执行。
- 简单的说：如果你单元测试代码使用了依赖注入就加上@RunWith，如果你不是手动new MockMvc对象就加上@AutoConfigureMockMvc

## 4.4 @SpringBootTest与@WebMvcTest区别
- @SpringBootTest注解告诉SpringBoot去寻找一个主配置类(例如带有@SpringBootApplication的配置类)，并使用它来启动Spring应用程序上下文。
SpringBootTest加载完整的应用程序并注入所有可能的bean，因此速度会很慢。
- @WebMvcTest注解主要用于controller层测试，只覆盖应用程序的controller层，HTTP请求和响应是Mock出来的，因此不会创建真正的网络连接。
WebMvcTest要快得多，因为我们只加载了应用程序的一小部分。

# 5. 使用swagger2构建发布API文档
## 5.1 引入依赖
```xml
<!-- https://mvnrepository.com/artifact/io.springfox/springfox-swagger2 -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>
<!-- https://mvnrepository.com/artifact/io.springfox/springfox-swagger-ui -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>

```
## 5.2 定义配置
```java
package com.zbb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2 {

    private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("springboot利用swagger构建api文档")
				.description("简单优雅的restfun风格")
				.termsOfServiceUrl("http://zhengbigbig.top")
				.version("1.0")
				.build();
    }

	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2)
                        .apiInfo(apiInfo())
                        .select()
                        //扫描basePackage包下面的“/rest/”路径下的内容作为接口文档构建的目标
                        .apis(RequestHandlerSelectors.basePackage("com.zbb"))
                        .paths(PathSelectors.regex("/rest/.*"))
                        .build();
	}
	
	
}
```
- @EnableSwagger2 注解表示开启SwaggerAPI文档相关的功能
- 在apiInfo方法中配置接口文档的title(标题)、描述、termsOfServiceUrl（服务协议）、版本等相关信息
- 在createRestApi方法中，basePackage表示扫描哪个package下面的Controller类作为API接口文档内容范围
- 在createRestApi方法中，paths表示哪一个请求路径下控制器映射方法，作为API接口文档内容范围

## 5.3 启动
- 启动项目，然后访问swagger-ui.html

## 5.3 书写接口文档
```java
@ApiOperation(value = "添加文章", notes = "添加新的文章", tags = "Article",httpMethod = "POST")
@ApiImplicitParams({
        @ApiImplicitParam(name = "title", value = "文章标题", required = true, dataType = "String"),
        @ApiImplicitParam(name = "content", value = "文章内容", required = true, dataType = "String"),
        @ApiImplicitParam(name = "author", value = "文章作者", required = true, dataType = "String")
})
@ApiResponses({
        @ApiResponse(code=200,message="成功",response=AjaxResponse.class),
})
@PostMapping("/article")
public @ResponseBody  AjaxResponse saveArticle(
        @RequestParam(value="title") String title,  //参数1
        @RequestParam(value="content") String content,//参数2
        @RequestParam(value="author") String author,//参数3
) {

```
## 5.4 常用注解
- @Api：用在Controller控制器类上
     属性tags="说明该类的功能及作用"

- @ApiOperation：用在Controller控制器类的请求的方法上
    value="说明方法的用途、作用"
    notes="方法的备注说明"

- @ApiImplicitParams：用在请求的方法上，表示一组参数说明
    @ApiImplicitParam：请求方法中参数的说明
        name：参数名
        value：参数的汉字说明、解释、用途
        required：参数是否必须传，布尔类型
        paramType：参数的类型，即参数存储位置或提交方式
            · header --> Http的Header携带的参数的获取：@RequestHeader
            · query --> 请求参数的获取：@RequestParam   （如上面的例子）
            · path（用于restful接口）--> 请求参数的获取：@PathVariable
            · body（不常用）
            · form（不常用）    
        dataType：参数类型，默认String，其它值dataType="Integer"       
        defaultValue：参数的默认值

- @ApiResponses：用在控制器的请求的方法上，对方法的响应结果进行描述
    @ApiResponse：用于表达一个响应信息
        code：数字，例如400
        message：信息，例如"请求参数没填好"
        response：响应结果封装类，如上例子中的AjaxResponse.class

- @ApiModel：value=“通常用在描述@RequestBody和@ResponseBody注解修饰的接收参数或响应参数实体类”
    @ApiModelProperty：value="实体类属性的描述"
    
## 5.5 生产环境下如何禁用swagger2
- 禁用方法1：使用注解@Profile({"dev","test"}) 表示在开发或测试环境开启，而在生产关闭。
- 禁用方法2：使用注解@ConditionalOnProperty(name = "swagger.enable", havingValue = "true") 然后在测试配置或者开发配置中 添加 swagger.enable = true 即可开启，生产环境不填则默认关闭Swagger.