package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ProductService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController @req
public class ProductController {
    private final ProductService prod()
    @GetMapping("/api/product/{id}")
    public List<Product> getAll() {
        return ProductService.getAll();
    }
    @PostMapping("/api/product")
    public Product Create(@RequestBody Product p) {
        return prodServ.Save(p);
    }
}