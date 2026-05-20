package br.com.fiap.locatech.locatech.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.ControllerAdvice;

import static java.awt.SystemColor.info;

@OpenAPIDefinition
@ControllerAdvice
public class OpenApiConfig {
    @Bean
    public OpenAPI locaTech(){
        return new OpenAPI().info(
                new Info().title("LocaTech API")
                        .description("Projeto desenvolvido durante o curso Spring MVC")
                        .version("1.0.0")
                        .license(new License().name("Apache 2.0").url("https:github.com/MichelleAyumi"))
        );
    }

}
