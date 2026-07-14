package com.fiap.techchallengue;

import com.fiap.techchallengue.application.ApiDtos.MenuItemRequest;
import com.fiap.techchallengue.application.ApiDtos.MenuItemResponse;
import com.fiap.techchallengue.application.ApiDtos.RestaurantRequest;
import com.fiap.techchallengue.application.ApiDtos.RestaurantResponse;
import com.fiap.techchallengue.application.ApiDtos.UserRequest;
import com.fiap.techchallengue.application.ApiDtos.UserResponse;
import com.fiap.techchallengue.application.ApiDtos.UserTypeRequest;
import com.fiap.techchallengue.application.ApiDtos.UserTypeResponse;
import com.fiap.techchallengue.application.exception.BusinessException;
import com.fiap.techchallengue.application.exception.ResourceNotFoundException;
import com.fiap.techchallengue.application.usecase.MenuItemService;
import com.fiap.techchallengue.application.usecase.RestaurantService;
import com.fiap.techchallengue.application.usecase.UserService;
import com.fiap.techchallengue.application.usecase.UserTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RestaurantManagementServiceTest {

    @Autowired
    private UserTypeService userTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuItemService menuItemService;

    @Test
    void deveCriarRestauranteCompletoComItemNoCardapio() {
        // Preparação dos dados
        UserTypeRequest userTypeRequest = new UserTypeRequest("Dono de Restaurante");
        UserTypeResponse userType = userTypeService.create(userTypeRequest);

        UserRequest userRequest = new UserRequest(
                "Yumi",
                "YUMI@EXAMPLE.COM",
                userType.id());
        UserResponse owner = userService.create(userRequest);

        RestaurantRequest restaurantRequest = new RestaurantRequest(
                "KiDelicia",
                "Rua Teste, 10",
                "Brasileira",
                LocalTime.of(11, 0),
                LocalTime.of(23, 0),
                owner.id());
        RestaurantResponse restaurant = restaurantService.create(restaurantRequest);

        MenuItemRequest menuItemRequest = new MenuItemRequest(
                "Churrasco",
                "Prato completo",
                new BigDecimal("39.90"),
                false,
                "/images/feijoada.jpg",
                restaurant.id());

        // Execução
        MenuItemResponse menuItem = menuItemService.create(menuItemRequest);
        List<MenuItemResponse> restaurantItems = menuItemService.list(restaurant.id());

        // Verificações
        assertThat(owner.email()).isEqualTo("yumi@example.com");
        assertThat(restaurant.owner().type().name()).isEqualTo("Dono de Restaurante");
        assertThat(restaurantItems).extracting(MenuItemResponse::id).containsExactly(menuItem.id());
        assertThat(menuItem.photoPath()).isEqualTo("/images/feijoada.jpg");
    }

    @Test
    void naoDeveExcluirTipoDeUsuarioQueEstaSendoUtilizado() {
        // Preparação dos dados
        UserTypeRequest typeRequest = new UserTypeRequest("Cliente");
        UserTypeResponse userType = userTypeService.create(typeRequest);

        UserRequest userRequest = new UserRequest(
                "Ka",
                "ka@example.com",
                userType.id());
        userService.create(userRequest);

        // Execução e verificação
        assertThatThrownBy(() -> userTypeService.delete(userType.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("socio");
    }

    @Test
    void naoDeveCriarRestauranteComDonoInexistente() {
        // Preparação dos dados
        Long unknownOwnerId = 999L;
        RestaurantRequest request = new RestaurantRequest(
                "Caseirinho",
                "Rua Test2",
                "Mineira",
                LocalTime.NOON,
                LocalTime.of(22, 0),
                unknownOwnerId);

        // Execução e verificação
        assertThatThrownBy(() -> restaurantService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
