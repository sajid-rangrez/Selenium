package com.journal.scrap.dao;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.journal.scrap.entities.Article;
import com.journal.scrap.entities.Product;

@Component
public class JournalApiService {

	public Long saveArticle(Article article) {
		RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:9090/save/article";


        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap the object in HttpEntity (Spring will auto-convert POJO to JSON)
        HttpEntity<Article> request = new HttpEntity<>(article, headers);

        // Call POST API
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        System.out.println("Status Code: " + response.getStatusCodeValue());
        System.out.println("Response Body: " + response.getBody());
		
		return (long) 1;
	}
	public Long saveArticles(List<Article> articles) {
		RestTemplate restTemplate = new RestTemplate();
		String apiUrl = "http://localhost:9090/save/articles";
		
		
		// Set headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		// Wrap the object in HttpEntity (Spring will auto-convert POJO to JSON)
		HttpEntity<List<Article>> request = new HttpEntity<>(articles, headers);
		
		// Call POST API
		ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
		
		System.out.println("Status Code: " + response.getStatusCodeValue());
		System.out.println("Response Body: " + response.getBody());
		
		return (long) 1;
	}
	
	public Long saveProduct(Product products) {
		RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:9090/save/product";


        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap the object in HttpEntity (Spring will auto-convert POJO to JSON)
        HttpEntity<Product> request = new HttpEntity<>(products, headers);

        // Call POST API
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        System.out.println("Status Code: " + response.getStatusCodeValue());
        System.out.println("Response Body: " + response.getBody());
		
		return (long) 1;
	}
	public Long saveProducts(List<Product> products) {
		RestTemplate restTemplate = new RestTemplate();
		String apiUrl = "http://localhost:9090/save/products";
		
		
		// Set headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		// Wrap the object in HttpEntity (Spring will auto-convert POJO to JSON)
		HttpEntity<List<Product>> request = new HttpEntity<>(products, headers);
		
		// Call POST API
		ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
		
		System.out.println("Status Code: " + response.getStatusCodeValue());
		System.out.println("Response Body: " + response.getBody());
		
		return (long) 1;
	}
}
