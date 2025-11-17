package com.locallit.scomed.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.journal.scrap.model.LocalLitMsRequest;
import com.locallit.scomed.service.ScoMedService;

@RestController
@RequestMapping("local-lit/scomed")
public class ScoMedController {
	
	@Autowired
	private ScoMedService scoMedService;

//	@PostMapping("pageInfo")
//	public String getPageInfo(@RequestBody LocalLitMsRequest requestModel) throws JsonMappingException, JsonProcessingException {
//		scoMedService.getFeed(requestModel);
//		return "Scraping Initiated";
//	}

	@PostMapping("/comments")
	public String getComments(@RequestBody LocalLitMsRequest requestModel) throws JsonMappingException, JsonProcessingException {
        scoMedService.getComments(requestModel);
		return "Scraping Initiated";
	}
	@PostMapping("/yt/comments")
	public String getYtComments(@RequestBody LocalLitMsRequest requestModel) throws JsonMappingException, JsonProcessingException {
		scoMedService.getYTComments(requestModel);
		return "Scraping Initiated";
	}
}
