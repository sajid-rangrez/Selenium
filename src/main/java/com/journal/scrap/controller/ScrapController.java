package com.journal.scrap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.journal.scrap.model.WebScrapingReqModel;
import com.journal.scrap.model.WebScrapingRespModel;
import com.journal.scrap.service.ScrapRestService;
import com.journal.scrap.service.SeleniumService;

@RestController
public class ScrapController {
	
	@Autowired
	private ScrapRestService restService;

	@Autowired
	private SeleniumService seleniumService;
	
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
	public WebScrapingRespModel start(@RequestBody WebScrapingReqModel requestModel) throws InterruptedException {
		return restService.startScraping(requestModel);
	}
}
