package dev.shrkptv.userservice.services.impl;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.UserCreateDTO;
import dev.shrkptv.userservice.dto.UserResponseDTO;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.exception.FailedRegistrationException;
import dev.shrkptv.userservice.exception.UserAlreadyExistsException;
import dev.shrkptv.userservice.exception.UserNotFoundByEmailException;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.mapper.UserMapper;
import dev.shrkptv.userservice.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Keycloak keycloak;

    @Value("${kc.realm}")
    private String realm;

    @Override
    @CachePut(value = "users", key = "#result.id")
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        User user = userMapper.toEntity(userCreateDTO);

        if (userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(user.getEmail());
        }

        UserRepresentation userRep = createKeycloakUserRepresentation(userCreateDTO);
        var response = keycloak.realm(realm).users().create(userRep);

        if (response.getStatus() != 201) {
            throw new FailedRegistrationException();
        }

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    private UserRepresentation createKeycloakUserRepresentation(UserCreateDTO userCreateDTO) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userCreateDTO.getEmail());
        user.setEmail(userCreateDTO.getEmail());
        user.setFirstName(userCreateDTO.getName());
        user.setLastName(userCreateDTO.getSurname());

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userCreateDTO.getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));
        return user;
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new UserNotFoundByIdException(id));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException(email));
        return userMapper.toDto(user);
    }


    @Override
    public List<UserResponseDTO> getUserList(List<Long> idList) {
        return userRepository.findUsersByIdIn(idList)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new UserNotFoundByIdException(id));
        if (userUpdateDTO.getEmail() != null
                && !userUpdateDTO.getEmail().equals(user.getEmail())) {
            userRepository.findUserByEmail(userUpdateDTO.getEmail())
                    .ifPresent(existing -> {
                        throw new UserAlreadyExistsException(userUpdateDTO.getEmail());
                    });
        }
        userMapper.updateEntityFromDto(userUpdateDTO, user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundByIdException(id);
        }

        userRepository.deleteUserById(id);
    }
}
