package com.example.caching;

import com.example.caching.dto.ProductDTO;
import com.example.caching.entity.Product;
import com.example.caching.repository.ProductRepository;
import com.example.caching.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SpringRedisCachingApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private TestRestTemplate restTemplate;

    private final static String BASE_URI = "/api/product";

    @Test
    void contextLoads() {
    }

    @Test
    void testProductService_Get_Product_By_Id_OK() {
        // Arrange
        Long id = 1L;

        var product = productRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Product not found for id: " + id));

        // Act
        var productForId = restTemplate.getForEntity(BASE_URI + "/" + id, ProductDTO.class);

        // Assert
        assert productForId != null;
        assert productForId.getStatusCode().is2xxSuccessful();
        assert productForId.getBody() != null;
        assert productForId.getBody().getId().equals(id);
        assert productForId.getBody().getName().equals(product.getName());
        assert productForId.getBody().getPrice().equals(product.getPrice());
        assert productForId.getBody().getDescription().equals(product.getDescription());
    }

    @Test
    void testProductService_Save_Product_OK() {
        // Arrange
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Product 1");
        productDTO.setPrice(100.0);
        productDTO.setDescription("Product 1 description");

        // Act
        var response = restTemplate
                .postForEntity(BASE_URI, productDTO, ProductDTO.class);

        // Assert
        assert response != null;
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().getId() != null;
        assert response.getBody().getName().equals(productDTO.getName());
        assert response.getBody().getPrice().equals(productDTO.getPrice());
        assert response.getBody().getDescription().equals(productDTO.getDescription());

    }

    @Test
    void testProductService_Update_Product_OK() {
        // Arrange
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setName("Product 1");
        productDTO.setPrice(120.0);
        productDTO.setDescription("Product 1 description");

        HttpEntity<ProductDTO> request = new HttpEntity<>(productDTO);

        var originalProduct = productRepository.findById(productDTO.getId()).orElseThrow(
                () -> new RuntimeException("Product not found for id: " + productDTO.getId()));

        // Act
        var response = restTemplate.exchange(BASE_URI + "/" + originalProduct.getId(),
               HttpMethod.PUT, request, ProductDTO.class);

        // Assert
        assert response != null;
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().getId().equals(productDTO.getId());
        assert response.getBody().getName().equals(productDTO.getName());
        assert response.getBody().getPrice().equals(productDTO.getPrice());
        assert response.getBody().getDescription().equals(productDTO.getDescription());
        assert !response.getBody().getPrice().equals(originalProduct.getPrice());

    }

    @Test
    void testProductService_Delete_Product_OK() {
        // Arrange
        Long id = 5L;

        productRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Product not found for id: " + id));

        // Act
        restTemplate.delete(BASE_URI+"/{id}", id);

        // Assert
        assert productRepository.findById(id).isEmpty();
    }

    @Test
    void testProductService_Get_Product_By_Id_Not_Found() {
        // Arrange
        Long id = 100L;
        // Act
        assertThrows(RuntimeException.class,
                () -> productService.getProductById(id));
    }

    //Test the cache by TTL (time-to-live) expiration of 10 seconds
    @Test
    void test_Product_Caching() throws InterruptedException {

        ProductDTO product = new ProductDTO();
        product.setName("Cached Product");
        product.setPrice(29.99);
        product.setDescription("This is a cached product");

        ProductDTO savedProduct = productService.saveProduct(product);

        // First request should hit the database
        var response1 = restTemplate.getForEntity(BASE_URI + "/" + savedProduct.getId(), ProductDTO.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);


        // Second request should return the cached version
        var response2 = restTemplate.getForEntity(BASE_URI + "/" + savedProduct.getId(), Product.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().getName()).isEqualTo("Cached Product");

        // Wait for the cache to expire (TTL + a small buffer)
        Thread.sleep(12000); // 11 seconds, assuming 10 second TTL

        // Update the product in the database
        savedProduct.setName("Updated Product");
        productService.updateProduct(savedProduct.getId(), savedProduct);

        // Third request should hit the database and return the updated product
        var response3 = restTemplate.getForEntity(BASE_URI + "/" + savedProduct.getId(), Product.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getBody().getName()).isEqualTo("Updated Product");
    }

}
