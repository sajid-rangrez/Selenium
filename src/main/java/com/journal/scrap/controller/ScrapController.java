package com.journal.scrap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journal.scrap.model.LocalLitMsRequest;
import com.journal.scrap.model.LocalLitMsResponse;
import com.journal.scrap.service.ScrapRestService;

@RestController
@RequestMapping("local-lit/scrap")
public class ScrapController {
	
	@Autowired
	private ScrapRestService restService;

//	@PostMapping("start")
//	public String start(@RequestBody JSONObject journalConfig) throws InterruptedException {
//		restService.startScraping(journalConfig);
//		return "Executed";
//	}
//	@PostMapping("start2")
//	public LocalLitMsResponse start(@RequestBody LocalLitMsRequest requestModel) throws InterruptedException {
//		return restService.startScraping(requestModel);
//	}
	@PostMapping("start")
	public String startInThread(@RequestBody LocalLitMsRequest requestModel) throws InterruptedException {
		
		return restService.startThread(requestModel);
	}
}
