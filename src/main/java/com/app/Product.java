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

class Product {
    private String name;
    private String price;
    private String manufacturer;

    public Product(String name, String price, String manufacturer) {
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getManufacturer() {
        return manufacturer;
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
                String productUrl = "https://999.md" + item.select("div.ads-list-photo-item-title a").attr("href");

                String manufacturer = fetchManufacturer(productUrl);

                productList.add(new Product(name, price, manufacturer));
            }

            page++;
//            break;
        }

        return productList;
    }

    private static String fetchManufacturer(String productUrl) throws IOException {

        Document productDoc = Jsoup.connect(productUrl).get();

        Element manufacturerElement = productDoc.selectFirst("li.m-value[itemprop=additionalProperty] span[itemprop=value] a");

        if (manufacturerElement != null) {
            return manufacturerElement.text();
        }

        return "Unknown";
    }
}