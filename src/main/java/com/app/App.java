package com.app;

import java.io.IOException;
import java.util.List;

import static com.app.Product.fetchProducts;

public class App 
{
    public static void main(String[] args) {
        try {
            FilteredProducts products = fetchProducts();
            System.out.println("Products:");
            System.out.println(products);
//            for (Product product : products) {
//                System.out.println("Name: " + product.getName() + ", Price: " + product.getPrice() + ", Manufacturer: " + product.getManufacturer());
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
