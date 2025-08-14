package com.journal.scrap.dao;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.journal.scrap.model.WebScrapingResponse;

@Component
public class JournalApiService {
	
	
	public void sentResponse(WebScrapingResponse scrapResponse) {
		RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://192.168.1.17:8080/ReTrans/api/pushWebScarpedArticles";


        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap the object in HttpEntity (Spring will auto-convert POJO to JSON)
        HttpEntity<WebScrapingResponse> request = new HttpEntity<>(scrapResponse, headers);

        // Call POST API
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        System.out.println("Status Code: " + response.getStatusCodeValue());
        System.out.println("Response Body: " + response.getBody());
		
		
	}
 
}
