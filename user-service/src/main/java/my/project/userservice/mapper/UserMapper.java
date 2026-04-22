package my.project.userservice.mapper;

import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.UpdateUserRequest;
import my.project.userservice.dto.UserBriefDto;
import my.project.userservice.dto.UserDto;
import my.project.userservice.dto.UserLookupDto;
import my.project.userservice.entity.UserEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", expression = "java(passwordEncoder.encode(req.password()))")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(RegistrationRequest req, @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateUserRequest req, @MappingTarget UserEntity user);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDto toDto(UserEntity user);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserLookupDto toLookupDto(UserEntity user);

    List<UserLookupDto> toLookupDto(List<UserEntity> users);

    UserBriefDto toBriefDto(UserEntity user);

    List<UserBriefDto> toBriefDto(List<UserEntity> users);
}