package com.app.lab2.controllers;

import com.app.lab2.models.Product;
import com.app.lab2.services.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public String showProducts(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "9") int size) {
        Page<Product> productPage = productService.getPaginatedProducts(page, size);

        for (Product product : productPage) {
            product.setName(URLDecoder.decode(product.getName(), StandardCharsets.UTF_8));
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "products";
    }

    @GetMapping("/createProduct")
    public String showForm(Model model) {
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new Product());
        }
        return "create";
    }

    @PostMapping("/createProduct")
    public String createProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/products";
    }

    @GetMapping("/editRow/{id}")
    public String editRow(@PathVariable Long id, Model model) {

        Product userData = productService.findById(id);
        userData.setName(URLDecoder.decode(userData.getName(), StandardCharsets.UTF_8));
        model.addAttribute("product", userData);
        return "edit";
    }

    @DeleteMapping("/deleteRow/{id}")
    public String deleteRow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/products";
    }

    @PutMapping("/updateRow/{id}")
    public String updateRow(@PathVariable Long id, @ModelAttribute Product userData, RedirectAttributes redirectAttributes) {

        Product existingData = productService.findById(id);
        existingData.setName(userData.getName());
        existingData.setManufacturer(userData.getManufacturer());
        existingData.setPrice(userData.getPrice());
        productService.editProduct(existingData);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/products";
    }

    @PostMapping("/uploadJson")
    public String uploadJsonFile(MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/createProduct";
        }

        System.out.println("HELLO");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Product> products = objectMapper.readValue(file.getInputStream(), new TypeReference<>() {
            });

            for (Product product : products) {
                productService.saveProduct(product);
            }

            redirectAttributes.addFlashAttribute("successFile", "File uploaded and products saved successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to process the file.");
        }

        return "redirect:/createProduct";
    }
}
