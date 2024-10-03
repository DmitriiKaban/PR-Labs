package com.app;

import java.io.IOException;
import java.util.List;

import static com.app.Product.fetchProducts;

public class App 
{
    public static void main(String[] args) {
        try {
            List<Product> products = fetchProducts();
            for (Product product : products) {
                System.out.println("Name: " + product.getName() + ", Price: " + product.getPrice());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
