package com.app;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.app.Request.fetchHttpsResponse;

class Product {
    private static final double DOLLARS_TO_LEI = 19.31;
    private String name;
    private Double price;
    private String manufacturer;

    public Product(String name, Double price, String manufacturer) {
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public static FilteredProducts fetchProducts() throws IOException {
        List<Product> productList = new ArrayList<>();
        int page = 1;
        while (true) {
            String htmlContent = fetchHttpsResponse(page);

            Document doc = Jsoup.parse(htmlContent);
            Elements items = doc.select("li.ads-list-photo-item");
            if (items.isEmpty()) {
                break;
            }

            for (Element item : items) {
                String name = item.select("div.ads-list-photo-item-title a").text();
                String price = item.select("div.ads-list-photo-item-price span").text();

                System.out.println("Name: " + name + ", Price: " + price);

                try {
                    validatePrice(price);
                } catch (IllegalArgumentException e) {
                    System.out.println("Skipping product: " + name + " because of invalid price");
                    continue;
                }

                try {
                    validateName(name);
                } catch (IllegalArgumentException e) {
                    System.out.println("Skipping product: " + name + " because of invalid manufacturer");
                    continue;
                }

                String productUrl = "https://999.md" + item.select("div.ads-list-photo-item-title a").attr("href");

                String manufacturer = fetchManufacturer(productUrl);
                Product product = new Product(name, parsePrice(price), manufacturer);
                System.out.println("Adding product: " + product);
                productList.add(product);
            }

            page++;
            break;
        }

        List<Product> filteredProducts = productList.stream()
                .filter(product -> product.price > 250)
                .collect(Collectors.toList());

        double totalPrice = sumPrices(productList);

        for (Product product : filteredProducts) {
            System.out.println("Name: " + product.getName() + ", Price: " + product.getPrice() + ", Manufacturer: " + product.getManufacturer());
        }

        return new FilteredProducts(filteredProducts, totalPrice);
    }

    public static double sumPrices(List<Product> productList) {
        return productList.stream()
                .map(product -> product.price)
                .reduce(0.0, Double::sum);
    }

    private static void validateName(String name) {
        if (name.matches(".*[a-zA-Z].*")) {
            throw new IllegalArgumentException("Name contains latin letters");
        }
    }

    private static Double parsePrice(String priceText) {
        String cleanedPrice = priceText.replaceAll("[^0-9]", "");

        if (!cleanedPrice.isEmpty()) {
            double price = Double.parseDouble(cleanedPrice);

            if (priceText.contains("леев")) {
                price = Math.round(price / DOLLARS_TO_LEI * 100.0) / 100.0;
            }

            return price;
        }
        return null;
    }


    private static String fetchManufacturer(String productUrl) throws IOException {

        Document productDoc = Jsoup.connect(productUrl).get();

        Element manufacturerElement = productDoc.selectFirst("li.m-value[itemprop=additionalProperty] span[itemprop=value] a");

        if (manufacturerElement != null) {
            return manufacturerElement.text();
        }

        return "Unknown";
    }

    private static void validatePrice(String price) {

        String cleanedPrice = price.replaceAll("[^\\d]", "");

        if (cleanedPrice.isEmpty()) {
            throw new IllegalArgumentException("Price is empty");
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", manufacturer='" + manufacturer + '\'' +
                '}';
    }

    public String toJson() {
        return String.format(
                "{\"name\": \"%s\", \"price\": %.2f, \"manufacturer\": \"%s\"}",
                name, price, manufacturer
        );
    }

    public String toXml() {
        return  "<Product>\n" +
                "           <name>" + name + "</name>\n" +
                "           <price>" + price + "</price>\n" +
                "           <manufacturer>" + manufacturer + "</manufacturer>\n" +
                "        </Product>";
    }
}