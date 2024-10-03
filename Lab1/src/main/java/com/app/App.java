package com.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.app.FilteredProducts.deserializeFromCustomForm;
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

            byte[] serialized = filteredProducts.serializeToCustomForm();
            System.out.println("Custom serialization: " + Arrays.toString(serialized));
            FilteredProducts deserialized = deserializeFromCustomForm(serialized);
            System.out.println("Custom deserialization: " + deserialized);

            System.out.println(filteredProducts.toXml());
            System.out.println(filteredProducts.toJson());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
