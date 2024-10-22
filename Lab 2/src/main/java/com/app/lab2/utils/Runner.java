package com.app.lab2.utils;

import com.app.lab2.services.ProductScanner;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

    private final ProductScanner productScanner;

    @Override
    public void run(ApplicationArguments args) {

//        System.out.println("Loading initial data in DB");
//        productScanner.importProducts();
    }
}
