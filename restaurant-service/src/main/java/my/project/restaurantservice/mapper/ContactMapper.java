package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.ContactDto;
import my.project.restaurantservice.entity.ContactEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ContactMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	ContactEntity toEntity(ContactDto contactDto);

	ContactDto toDto(ContactEntity contactEntity);


	List<ContactEntity> toEntity(List<ContactDto> contactDtoList);

	List<ContactDto> toDto(List<ContactEntity> contactEntityList);
}
