package my.project.userservice.util;

import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.entity.UserEntity;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", expression = "java(passwordEncoder.encode(req.password()))")
    @Mapping(target = "role", constant = "ROLE_USER")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(RegistrationRequest req, @Context PasswordEncoder passwordEncoder);
}
