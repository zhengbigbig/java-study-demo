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
// @EnableSwagger2 注解表示开启SwaggerAPI文档相关的功能
public class Swagger2 {

    private ApiInfo apiInfo() {
    	// 在apiInfo方法中配置接口文档的title(标题)、描述、termsOfServiceUrl（服务协议）、版本等相关信息
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