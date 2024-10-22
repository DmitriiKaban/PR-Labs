package com.app.lab2.services;

import com.app.lab2.models.Product;
import com.app.lab2.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public void saveProduct(Product product) {
        product.setName(escapeSpecialCharacters(product.getName()));
        productRepository.save(product);
    }

    public Page<Product> getPaginatedProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void updateProduct(Product product) {
        productRepository.save(product);
    }

    public void saveAllProducts(List<Product> products) {

        for (Product product : products) {
            product.setName(escapeSpecialCharacters(product.getName()));
        }

        productRepository.saveAll(products);
    }

    public String escapeSpecialCharacters(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public void editProduct(Product existingData) {
        productRepository.save(existingData);
    }
}
