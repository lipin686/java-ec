package com.example.demo.mapper;

import com.example.demo.dto.request.backend.UpdateProductRequest;
import com.example.demo.dto.response.frontend.ProductResponse;
import com.example.demo.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromDto(UpdateProductRequest dto, @MappingTarget Product entity);

    // entity 轉前台商品列表 response
    ProductResponse toProductResponse(Product product);
}
