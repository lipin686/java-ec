package com.example.demo.mapper;

import com.example.demo.dto.request.backend.UpdateProductRequest;
import com.example.demo.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromDto(UpdateProductRequest dto, @MappingTarget Product entity);
}

