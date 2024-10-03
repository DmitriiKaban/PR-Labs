package com.app;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.app.Request.makeRequestAndSaveToFile;

public class Product {
    private String name;
    private String price;

    public Product(String name, String price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public static List<Product> fetchProducts() throws IOException {
        List<Product> productList = new ArrayList<>();
        int page = 1;

        while (true) {
            File tempFile = makeRequestAndSaveToFile(page);
            if (tempFile == null) {
                break;
            }

            Document doc = Jsoup.parse(tempFile, "UTF-8");
            Elements items = doc.select("li.ads-list-photo-item");
            if (items.isEmpty()) {
                break;
            }

            for (Element item : items) {
                String name = item.select("div.ads-list-photo-item-title a").text();
                String price = item.select("div.ads-list-photo-item-price span").text();
                productList.add(new Product(name, price));
            }

            page++;
        }

        return productList;
    }
}