package com.app.lab3.models;

//import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Table(name = "products")
//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    public static final double DOLLARS_TO_LEI = 19.31;

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(nullable = false)
    private Long id;

//    @Column(name = "name", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String name;

//    @Column
    private Double price;

//    @Column
    private String manufacturer;

    public Product(String name, Double aDouble, String manufacturer) {
        this.name = name;
        this.price = aDouble;
        this.manufacturer = manufacturer;
    }
}

