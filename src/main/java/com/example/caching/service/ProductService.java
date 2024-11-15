package com.example.caching.service;


import com.example.caching.dto.ProductDTO;
import com.example.caching.mappings.ProductMapper;
import com.example.caching.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProductById(Long id) {
        return ProductMapper.toProductDTO(productRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found for id: " + id)));
    }


    public ProductDTO saveProduct(ProductDTO productDTO) {
        return ProductMapper.toProductDTO(productRepository
                .save(ProductMapper.toProduct(productDTO)));
    }

    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Could not delete product.Product not found for id: " + id);
        }
        productRepository.deleteById(id);
    }

    public ProductDTO updateProduct(ProductDTO productDTO) {
        if (!productRepository.existsById(productDTO.getId())) {
            throw new RuntimeException("Could not update product.Product not found for id: " + productDTO.getId());
        }
        return ProductMapper
                .toProductDTO(productRepository
                        .save(ProductMapper.toProduct(productDTO)));
    }
}
