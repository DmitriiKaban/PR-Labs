package com.app.lab2.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "products")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    public static final double DOLLARS_TO_LEI = 19.31;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column
    private String name;

    @Column
    private Double price;

    @Column
    private String manufacturer;

    public Product(String name, Double aDouble, String manufacturer) {
        this.name = name;
        this.price = aDouble;
        this.manufacturer = manufacturer;
    }
}

