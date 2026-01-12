package my.project.userservice.util;

import my.project.userservice.dto.UserProfileResponse;
import my.project.userservice.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserProfileResponse toResponse(UserEntity user);
}
