package com.app;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Request {

    public static List<Product> fetchProducts() throws IOException {
        List<Product> productList = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = "https://999.md/ru/list/computers-and-office-equipment/laptops?page=" + page;
            Document doc = Jsoup.connect(url).get();

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

