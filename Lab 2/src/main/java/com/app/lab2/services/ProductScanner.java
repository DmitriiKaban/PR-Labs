package com.app.lab2.services;

import com.app.lab2.models.Product;
import com.app.lab2.utils.Scraper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductScanner {

    private final Scraper scraper;

    @Lazy
    private final ProductService productService;

    public void importProducts() {
        List<Product> productList = scraper.getProducts();
        System.out.println("Importing products in DB");
        System.out.println(productList);
        productService.saveAllProducts(productList);
    }
}
