package com.journal.scrap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journal.scrap.service.SeleniumService;

@RestController
public class ScrapController {

	@Autowired
	private SeleniumService seleniumService;

	@GetMapping("/run")
	public String runSeleniumTest() throws InterruptedException {
		seleniumService.startScraping();
		return "";
	}
}
