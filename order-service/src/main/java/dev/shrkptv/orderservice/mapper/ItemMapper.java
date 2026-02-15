package dev.shrkptv.orderservice.mapper;

import dev.shrkptv.orderservice.database.entity.Item;
import dev.shrkptv.orderservice.dto.ItemResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemResponseDTO toDto(Item item);
    List<ItemResponseDTO> toDtoList(List<Item> items);
}