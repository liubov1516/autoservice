package com.example.autoservice.mapper;

import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.entity.RepairOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// MapStruct сам генерує реалізацію цього інтерфейсу під час збірки
@Mapper(componentModel = "spring")
public interface RepairOrderMapper {

    RepairOrderDto toDto(RepairOrder entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    RepairOrder toEntity(RepairOrderDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(RepairOrderDto dto, @MappingTarget RepairOrder entity);
}
