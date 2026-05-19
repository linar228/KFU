package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.demo.properties.JwtProperties;
import com.example.demo.properties.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, JwtProperties.class})
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
