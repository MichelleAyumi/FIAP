package br.com.fiap.restauranteapi.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUserAndSearchByNameWithoutExposingPassword() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cliente Integracao",
                                  "email": "cliente.integracao@example.com",
                                  "login": "cliente.integracao",
                                  "password": "123456",
                                  "address": "Rua Teste, 10",
                                  "type": "CLIENTE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.lastModifiedAt").exists());

        mockMvc.perform(get("/api/v1/users?name=Integracao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("cliente.integracao@example.com"));
    }

    @Test
    void shouldReturnProblemDetailForDuplicatedEmail() throws Exception {
        String body = """
                {
                  "name": "Duplicado",
                  "email": "duplicado@example.com",
                  "login": "duplicado1",
                  "password": "123456",
                  "address": "Rua Teste, 10",
                  "type": "CLIENTE"
                }
                """;

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("duplicado1", "duplicado2")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Recurso duplicado"));
    }
}
