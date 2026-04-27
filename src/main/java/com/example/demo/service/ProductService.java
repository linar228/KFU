package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository prodRep;
    public List<Product> getAll()
    {
        return prodRep.findAll();
    }
    public Product create(Product product)
    {
        return prodRep.save(product);
    }
}
