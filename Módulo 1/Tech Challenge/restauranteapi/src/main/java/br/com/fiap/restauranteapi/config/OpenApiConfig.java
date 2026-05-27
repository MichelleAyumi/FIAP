package br.com.fiap.restauranteapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Usuarios API")
                        .version("v1")
                        .description("API para cadastro, atualizacao, busca, troca de senha e validacao de login de usuarios."));
    }
}
