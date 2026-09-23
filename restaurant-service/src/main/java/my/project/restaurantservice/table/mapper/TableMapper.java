package my.project.restaurantservice.table.mapper;

import my.project.restaurantservice.internal.dto.BookingTableDto;
import my.project.restaurantservice.table.dto.TableDto;
import my.project.restaurantservice.table.dto.TableLayoutItemRequest;
import my.project.restaurantservice.table.entity.TableEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TableMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TableEntity toEntity(TableDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget TableEntity entity, TableDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "positionX", source = "positionX")
    @Mapping(target = "positionY", source = "positionY")
    @Mapping(target = "markerSize", source = "markerSize")
    void updateLayout(@MappingTarget TableEntity entity, TableLayoutItemRequest item);

    TableDto toDto(TableEntity entity);

    List<TableDto> toDto(List<TableEntity> entity);

    BookingTableDto toBookingDto(TableEntity entity);
}
