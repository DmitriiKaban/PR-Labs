package com.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    public FilteredProducts(List<Product> filteredProducts, double totalPrice, LocalDateTime timestamp) {
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

    public byte[] serializeToCustomForm() {
        StringBuilder dataBuilder = new StringBuilder();

        for (Product product : filteredProducts) {
            dataBuilder.append(new String(product.serializeToCustomForm())).append(";"); // Separate products with a semicolon
        }

        dataBuilder.append("totalPrice=").append(totalPrice).append(";");
        dataBuilder.append("timestamp=").append(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(";");

        return dataBuilder.toString().getBytes();
    }

    public static FilteredProducts deserializeFromCustomForm(byte[] serializedDataBytes) {
        String data = new String(serializedDataBytes);
        String[] parts = data.split(";");

        List<Product> filteredProducts = new ArrayList<>();
        for (int i = 0; i < parts.length - 2; i++) { // Last two parts are totalPrice and timestamp
            filteredProducts.add(Product.deserializeFromCustomForm(parts[i].getBytes()));
        }

        double totalPrice = Double.parseDouble(parts[parts.length - 2].split("=")[1]);
        LocalDateTime timestamp = LocalDateTime.parse(parts[parts.length - 1].split("=")[1]);

        return new FilteredProducts(filteredProducts, totalPrice, timestamp);
    }
}
