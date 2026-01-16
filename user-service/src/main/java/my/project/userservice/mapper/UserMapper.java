package my.project.userservice.mapper;

import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.UserProfileResponse;
import my.project.userservice.entity.UserEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", expression = "java(passwordEncoder.encode(req.password()))")
    @Mapping(target = "role", constant = "ROLE_USER")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(RegistrationRequest req, @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserProfileResponse toResponse(UserEntity user);
}