package com.journal.scrap.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.journal.scrap.model.LocalLitMsResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class JournalApiService {
	
	@Value("local.lit.api.endpoint")
	private String apiUrl;
	
	public void sentResponse(LocalLitMsResponse scrapResponse) {
		RestTemplate restTemplate = new RestTemplate();


        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap the object in HttpEntity (Spring will auto-convert POJO to JSON)
        HttpEntity<LocalLitMsResponse> request = new HttpEntity<>(scrapResponse, headers);

        // Call POST API
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        System.out.println("Status Code: " + response.getStatusCodeValue());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("data sent");
        
		
		
	}
 
}
