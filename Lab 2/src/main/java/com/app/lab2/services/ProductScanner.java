package com.app.lab2.services;

import com.app.lab2.models.Product;
import com.app.lab2.utils.Scraper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductScanner {

    private final Scraper scraper;

    public void importProducts() {
        List<Product> productList = scraper.getProducts();
    }
}
