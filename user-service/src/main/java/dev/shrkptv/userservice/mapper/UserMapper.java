package dev.shrkptv.userservice.mapper;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.dto.UserCreateDTO;
import dev.shrkptv.userservice.dto.UserResponseDTO;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface UserMapper {
    User toEntity(UserCreateDTO dto);
    UserResponseDTO toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserUpdateDTO userUpdateDTO, @MappingTarget User user);
}
