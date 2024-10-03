package com.app;

import java.time.LocalDateTime;
import java.util.List;

public class FilteredProducts {

    private List<Product> filteredProducts;
    private double totalPrice;
    private LocalDateTime timestamp;

    public FilteredProducts(List<Product> filteredProducts, double totalPrice) {
        this.filteredProducts = filteredProducts;
        this.totalPrice = totalPrice;
        this.timestamp = LocalDateTime.now();  // Capturing the UTC timestamp
    }

    public List<Product> getFilteredProducts() {
        return filteredProducts;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "FilteredProductData{" +
                "filteredProducts=" + filteredProducts +
                ", totalPrice=" + totalPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}
