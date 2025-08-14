package com.journal.scrap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.journal.scrap.model.WebScrapingRequest;
import com.journal.scrap.model.WebScrapingResponse;
import com.journal.scrap.service.ScrapRestService;
import com.journal.scrap.service.SeleniumServiceOld;

@RestController
public class ScrapController {
	
	@Autowired
	private ScrapRestService restService;

	@Autowired
	private SeleniumServiceOld seleniumService;
	
	@GetMapping("/run")
	public String runSeleniumTest() throws InterruptedException {
		seleniumService.startScraping();
		return "";
	}
	
//	@PostMapping("start")
//	public String start(@RequestBody JSONObject journalConfig) throws InterruptedException {
//		restService.startScraping(journalConfig);
//		return "Executed";
//	}
	@PostMapping("start2")
	public WebScrapingResponse start(@RequestBody WebScrapingRequest requestModel) throws InterruptedException {
		return restService.startScraping(requestModel);
	}
	@PostMapping("start")
	public String startInThread(@RequestBody WebScrapingRequest requestModel) throws InterruptedException {
		
		return restService.startThread(requestModel);
	}
}
