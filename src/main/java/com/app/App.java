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

            FilteredProducts filteredProducts = new FilteredProducts(products.getFilteredProducts(), products.getTotalPrice());
            System.out.println("Filtered products:");

            System.out.println(filteredProducts.toJson());
            System.out.println(filteredProducts.toXml());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
