package com.app;

import java.io.IOException;
import java.util.Arrays;

import static com.app.Product.fetchProducts;

public class App 
{
    public static void main(String[] args) {
        try {
            FilteredProducts products = fetchProducts();
//            System.out.println("Products:");
            System.out.println(products);

            FilteredProducts filteredProducts = new FilteredProducts(products.getFilteredProducts(), products.getTotalPrice());
            String serialized = filteredProducts.serialize();
            System.out.println("Custom serialization: " + serialized);
//            FilteredProducts deserialized = new FilteredProducts();
            FilteredProducts deserialized = FilteredProducts.deserialize(serialized);
            System.out.println("Custom deserialization: " + deserialized);

            System.out.println(filteredProducts.toXml());
            System.out.println(filteredProducts.toJson());

            Product product = new Product("Laptop", 1000.0, "Apple");
            System.out.println("Product:");
            System.out.println(product);
            System.out.println("Custom serialization: " + product.serialize());
            System.out.println("Custom deserialization: " + Product.deserialize(product.serialize()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
