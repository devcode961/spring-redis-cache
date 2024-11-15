package com.example.caching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductDTO implements Serializable {
    private Long id;
    private String name;
    private Double price;
    private String description;
}
