package com.fiap.techchallengue.service;

import com.fiap.techchallengue.api.ApiDtos.*;
import com.fiap.techchallengue.domain.*;
import com.fiap.techchallengue.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RestaurantManagementService {
    private final UserTypeRepository types;
    private final UserRepository users;
    private final RestaurantRepository restaurants;
    private final MenuItemRepository menuItems;

    public RestaurantManagementService(UserTypeRepository types,
                                       UserRepository users,
                                       RestaurantRepository restaurants,
                                       MenuItemRepository menuItems) {
        this.types=types;
        this.users=users;
        this.restaurants=restaurants;
        this.menuItems=menuItems;

    }

    public UserTypeResponse createType(UserTypeRequest r) { S
        tring name=r.name().trim();
        if(types.existsByNameIgnoreCase(name)) {

            throw new BusinessException("Tipo de usuário já cadastrado");

        }

        return typeDto(types.save(new UserType(name)));

    }

    @Transactional(readOnly=true)
    public List<UserTypeResponse> listTypes(){
        return types.findAll().stream().map(this::typeDto).toList();
    }

    @Transactional(readOnly=true)
    public UserTypeResponse getType(Long id){
        return typeDto(findType(id));
    }

    public UserTypeResponse updateType(Long id, UserTypeRequest r){
        UserType t=findType(id);
        String name=r.name().trim();

        if(!t.getName().equalsIgnoreCase(name)&&types.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Tipo de usuário já cadastrado");
            t.setName(name);
            return typeDto(t);
        }

    public void deleteType(Long id){
            findType(id);
        }

        if(users.countByTypeId(id)>0) {
            throw new BusinessException("Tipo de usuário está associado a usuários");
        }

        types.deleteById(id);
    }

    public UserResponse createUser(UserRequest r){

        String email=r.email().trim().toLowerCase();

        if(users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("E-mail já cadastrado");
        }

        return userDto(users.save(new User(r.name().trim(),email,findType(r.typeId()))));
    }

    @Transactional(readOnly=true)
    public List<UserResponse> listUsers(){

        return users.findAll().stream().map(this::userDto).toList();
    }

    @Transactional(readOnly=true)
    public UserResponse getUser(Long id){
        return userDto(findUser(id));
    }

    public UserResponse updateUser(Long id,UserRequest r){
        User u=findUser(id);
        String email=r.email().trim().toLowerCase();

        if(!u.getEmail().equalsIgnoreCase(email)&&users.existsByEmailIgnoreCase(email)){
            throw new BusinessException("E-mail já cadastrado");
        }

        u.update(r.name().trim(),email,findType(r.typeId()));
        return userDto(u);
    }

    public void deleteUser(Long id){findUser(id);
        if(restaurants.countByOwnerId(id)>0) {
            throw new BusinessException("Usuário é dono de restaurante");
        }

        users.deleteById(id);
    }


    public RestaurantResponse createRestaurant(RestaurantRequest r){

        validateHours(r);
        return restaurantDto(restaurants.save(
                new Restaurant(r.name().trim(),r.address().trim(),r.cuisineType().trim(),r.openingTime(),r.closingTime(),findUser(r.ownerId()))));
    }

    @Transactional(readOnly=true)
    public List<RestaurantResponse> listRestaurants(){
        return restaurants.findAll().stream().map(this::restaurantDto).toList();
    }

    @Transactional(readOnly=true)
    public RestaurantResponse getRestaurant(Long id){
        return restaurantDto(findRestaurant(id));
    }

    public RestaurantResponse updateRestaurant(Long id,RestaurantRequest r){
        validateHours(r);
        Restaurant x=findRestaurant(id);

        x.update(r.name().trim(),r.address().trim(),r.cuisineType().trim(),r.openingTime(),r.closingTime(),findUser(r.ownerId()));
        return restaurantDto(x);

    }
    public void deleteRestaurant(Long id){
        findRestaurant(id);

        if(menuItems.countByRestaurantId(id)>0) {
            throw new BusinessException("Restaurante possui itens no cardápio");
        }

        restaurants.deleteById(id);
    }

    public MenuItemResponse createMenuItem(MenuItemRequest r){
        return menuDto(menuItems.save(
                new MenuItem(r.name().trim(),r.description().trim(),r.price(),r.available(),r.dineInOnly(),r.photoUrl(),findRestaurant(r.restaurantId()))));
    }

    @Transactional(readOnly=true)
    public List<MenuItemResponse> listMenuItems(Long restaurantId){

        if(restaurantId==null){
            return menuItems.findAll().stream().map(this::menuDto).toList();
        }

        findRestaurant(restaurantId);

        return menuItems.findAllByRestaurantId(restaurantId).stream().map(this::menuDto).toList();

    }

    @Transactional(readOnly=true)
    public MenuItemResponse getMenuItem(Long id){
        return menuDto(findMenuItem(id));
    }

    public MenuItemResponse updateMenuItem(Long id,MenuItemRequest r){
        MenuItem i=findMenuItem(id);
        i.update(r.name().trim(),r.description().trim(),r.price(),r.available(),r.dineInOnly(),r.photoUrl(),findRestaurant(r.restaurantId()));
        return menuDto(i);
    }

    public void deleteMenuItem(Long id){
        menuItems.delete(findMenuItem(id));
    }

    private void validateHours(RestaurantRequest r){
        if(r.openingTime().equals(r.closingTime())){
            throw new BusinessException("Horários de abertura e fechamento devem ser diferentes");
        }
    }

    private UserType findType(Long id){
        return types.findById(id).orElseThrow(()->new ResourceNotFoundException("Tipo de usuário não encontrado"));
    }

    private User findUser(Long id){
        return users.findById(id).orElseThrow(()->new ResourceNotFoundException("Usuário não encontrado"));
    }

    private Restaurant findRestaurant(Long id){
        return restaurants.findById(id).orElseThrow(()->new ResourceNotFoundException("Restaurante não encontrado"));
    }

    private MenuItem findMenuItem(Long id){
        return menuItems.findById(id).orElseThrow(()->new ResourceNotFoundException("Item de cardápio não encontrado"));
    }

    private UserTypeResponse typeDto(UserType t){
        return new UserTypeResponse(t.getId(),t.getName());
    }

    private UserResponse userDto(User u){
        return new UserResponse(u.getId(),u.getName(),u.getEmail(),typeDto(u.getType()));
    }

    private RestaurantResponse restaurantDto(Restaurant r){
        return new RestaurantResponse(r.getId(),r.getName(),r.getAddress(),r.getCuisineType(),r.getOpeningTime(),r.getClosingTime(),userDto(r.getOwner()));
    }

    private MenuItemResponse menuDto(MenuItem i){
        return new MenuItemResponse(i.getId(),i.getName(),i.getDescription(),i.getPrice(),i.isAvailable(),i.isDineInOnly(),i.getPhotoUrl(),i.getRestaurant().getId());
    }

}
