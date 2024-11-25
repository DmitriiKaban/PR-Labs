package com.app.scraperlab3.services;

import com.app.scraperlab3.models.Product;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.app.scraperlab3.models.Product.DOLLARS_TO_LEI;


@Component
@RequiredArgsConstructor
public class Scraper {

    private final Requests requests;

    public List<Product> getProducts() {

        List<Product> productList = new ArrayList<>();
        int page = 1;
        int maxPages = 2;
        while (page < maxPages) {
            String htmlContent;
            try {
                htmlContent = requests.fetchHttpsResponse(page);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Document doc = Jsoup.parse(htmlContent);
            Elements items = doc.select("li.ads-list-photo-item");
            if (items.isEmpty()) {
                break;
            }

            for (Element item : items) {
                String name = item.select("div.ads-list-photo-item-title a").text();
                String price = item.select("div.ads-list-photo-item-price span").text();

                try {
                    validatePrice(price);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                try {
                    validateName(name);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                String productUrl = "https://999.md" + item.select("div.ads-list-photo-item-title a").attr("href");

                String manufacturer;
                try {
                    manufacturer = fetchManufacturer(productUrl);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Product product = new Product(name, parsePrice(price), manufacturer);
                productList.add(product);
            }

            page++;
        }

        return productList;
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

    private static void validateName(String name) {
        if (name.matches(".*[а-яА-ЯЁёЇїІіЄєҐґ].*")) {
            throw new IllegalArgumentException("Name contains Cyrillic letters");
        }
    }


    private static void validatePrice(String price) {

        String trimmedPrice = price.trim();
        String cleanedPrice = trimmedPrice.replaceAll("[^\\d.]", "");

        if (cleanedPrice.isEmpty()) {
            throw new IllegalArgumentException("Price is empty");
        }

        try {
            double numericPrice = Double.parseDouble(cleanedPrice);
            if (numericPrice <= 0) {
                throw new IllegalArgumentException("Price is not positive");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format");
        }
    }
}

