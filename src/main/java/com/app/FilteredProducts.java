package com.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("    \"filteredProducts\": [\n");

        for (int i = 0; i < filteredProducts.size(); i++) {
            jsonBuilder.append("        ").append(filteredProducts.get(i).toJson());
            if (i < filteredProducts.size() - 1) {
                jsonBuilder.append(",\n");
            }
        }

        jsonBuilder.append("\n    ],\n");
        jsonBuilder.append("    \"totalPrice\": ").append(totalPrice).append(",\n");
        jsonBuilder.append("    \"timestamp\": \"").append(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\"\n");
        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }

    public String toXml() {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<FilteredProducts>\n");

        xmlBuilder.append("    <Products>\n");
        for (Product product : filteredProducts) {
            xmlBuilder.append("        ").append(product.toXml()).append("\n");
        }
        xmlBuilder.append("    </Products>\n");

        xmlBuilder.append("    <totalPrice>").append(totalPrice).append("</totalPrice>\n");
        xmlBuilder.append("    <time>").append(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</timestamp>\n");
        xmlBuilder.append("</FilteredProducts>");

        return xmlBuilder.toString();
    }
}
