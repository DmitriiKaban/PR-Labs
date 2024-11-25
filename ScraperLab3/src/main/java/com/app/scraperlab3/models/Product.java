package com.app.scraperlab3.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    public static final double DOLLARS_TO_LEI = 19.31;

    private Long id;

    private String name;

    private Double price;

    private String manufacturer;

    public Product(String name, Double aDouble, String manufacturer) {
        this.name = name;
        this.price = aDouble;
        this.manufacturer = manufacturer;
    }
}

