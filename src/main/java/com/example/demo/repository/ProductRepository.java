package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product/long> {
    Optional<Product> findByTitle(String title);
    @Query("SELECT p FROM Product.P WHERE p.price = (SELECT MAX(price) FROM Product)")
    List<Product> findMostExpensive();
}
