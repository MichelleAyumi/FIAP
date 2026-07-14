package com.fiap.techchallengue;

import com.fiap.techchallengue.infrastructure.persistence.JpaMenuItemRepository;
import com.fiap.techchallengue.infrastructure.persistence.JpaRestaurantRepository;
import com.fiap.techchallengue.infrastructure.persistence.JpaUserRepository;
import com.fiap.techchallengue.infrastructure.persistence.JpaUserTypeRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaMenuItemRepository menuItemRepository;

    @Autowired
    private JpaRestaurantRepository restaurantRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaUserTypeRepository userTypeRepository;

    @BeforeEach
    void limparBancoDeDadosAntesDeCadaTeste() {
        menuItemRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
        userTypeRepository.deleteAll();
    }

    @Test
    void devePublicarDocumentacaoOpenApiComOsEndpointsGet() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Gestão de Restaurantes"))
                .andExpect(jsonPath("$.paths['/api/user-types'].get").exists())
                .andExpect(jsonPath("$.paths['/api/users'].get").exists())
                .andExpect(jsonPath("$.paths['/api/restaurants'].get").exists())
                .andExpect(jsonPath("$.paths['/api/menu-items'].get").exists());
    }

    @Test
    void deveExecutarCadastroConsultaAtualizacaoEExclusaoPelaApi() throws Exception {
        // Cadastra um tipo de usuário
        String userTypeJson = "{\"name\":\"Dono de Restaurante\"}";

        MvcResult userTypeResult = mockMvc.perform(post("/api/user-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userTypeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dono de Restaurante"))
                .andReturn();

        long userTypeId = getIdFromResult(userTypeResult);

        // Cadastra um usuário
        String userJson = ("{\"name\":\"Ana\",\"email\":\"ana@example.com\"," +
                "\"typeId\":%d}").formatted(userTypeId);

        MvcResult userResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn();

        long userId = getIdFromResult(userResult);

        // Cadastra um restaurante
        String restaurantJson = ("{\"name\":\"Sabor\",\"address\":\"Rua A, 10\"," +
                "\"cuisineType\":\"Brasileira\",\"openingTime\":\"11:00:00\"," +
                "\"closingTime\":\"23:00:00\",\"ownerId\":%d}").formatted(userId);

        MvcResult restaurantResult = mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restaurantJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner.id").value(userId))
                .andReturn();

        long restaurantId = getIdFromResult(restaurantResult);

        // Cadastra um item no cardápio
        String menuItemJson = ("{\"name\":\"Feijoada\",\"description\":\"Prato completo\"," +
                "\"price\":39.90,\"dineInOnly\":true," +
                "\"photoPath\":\"/images/feijoada.jpg\"," +
                "\"restaurantId\":%d}").formatted(restaurantId);

        MvcResult menuItemResult = mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(menuItemJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dineInOnly").value(true))
                .andReturn();

        long menuItemId = getIdFromResult(menuItemResult);

        // Consulta os itens do restaurante
        mockMvc.perform(get("/api/menu-items")
                        .param("restaurantId", String.valueOf(restaurantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(menuItemId));

        // Atualiza o item do cardápio
        String updatedMenuItemJson = ("{\"name\":\"Feijoada premium\"," +
                "\"description\":\"Prato completo\",\"price\":49.90," +
                "\"dineInOnly\":false,\"photoPath\":\"/images/premium.jpg\"," +
                "\"restaurantId\":%d}").formatted(restaurantId);

        mockMvc.perform(put("/api/menu-items/{id}", menuItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMenuItemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Feijoada premium"));

        // Confirma os recursos consultados
        mockMvc.perform(get("/api/user-types/{id}", userTypeId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isOk());

        // Exclui os recursos na ordem
        mockMvc.perform(delete("/api/menu-items/{id}", menuItemId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/user-types/{id}", userTypeId))
                .andExpect(status().isNoContent());
    }

    private long getIdFromResult(MvcResult result) throws Exception {
        String responseJson = result.getResponse().getContentAsString();
        Number id = JsonPath.read(responseJson, "$.id");

        return id.longValue();
    }
}
