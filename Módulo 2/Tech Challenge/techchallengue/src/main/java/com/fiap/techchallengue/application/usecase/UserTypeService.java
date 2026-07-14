package com.fiap.techchallengue.application.usecase;

import com.fiap.techchallengue.application.ApiDtos.UserTypeRequest;
import com.fiap.techchallengue.application.ApiDtos.UserTypeResponse;
import com.fiap.techchallengue.application.exception.BusinessException;
import com.fiap.techchallengue.application.exception.ResourceNotFoundException;
import com.fiap.techchallengue.application.port.out.UserRepositoryPort;
import com.fiap.techchallengue.application.port.out.UserTypeRepositoryPort;
import com.fiap.techchallengue.domain.model.UserType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserTypeService {
    private final UserTypeRepositoryPort userTypes;
    private final UserRepositoryPort users;

    public UserTypeService(UserTypeRepositoryPort userTypes, UserRepositoryPort users) {
        this.userTypes = userTypes;
        this.users = users;
    }

    public UserTypeResponse create(UserTypeRequest request) {
        String name = request.name().trim();
        ensureNameAvailable(name, null);

        return toResponse(userTypes.save(new UserType(name)));
    }

    @Transactional(readOnly = true)
    public List<UserTypeResponse> list() {

        return userTypes.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserTypeResponse get(Long id) {

        return toResponse(findEntity(id));
    }

    public UserTypeResponse update(Long id, UserTypeRequest request) {
        UserType type = findEntity(id);
        String name = request.name().trim();
        ensureNameAvailable(name, type);
        type.setName(name);

        return toResponse(type);
    }

    public void delete(Long id) {
        findEntity(id);
        if (users.countByTypeId(id) > 0) {
            throw new BusinessException("Tipo de usuário está associado a usuários");
        }

        userTypes.deleteById(id);
    }

    public UserType findEntity(Long id) {
        return userTypes.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário não encontrado"));
    }

    public UserTypeResponse toResponse(UserType type) {

        return new UserTypeResponse(type.getId(), type.getName());
    }

    private void ensureNameAvailable(String name, UserType current) {
        boolean unchanged = current != null && current.getName().equalsIgnoreCase(name);

        if (!unchanged && userTypes.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Tipo de usuário já cadastrado");
        }
    }
}
