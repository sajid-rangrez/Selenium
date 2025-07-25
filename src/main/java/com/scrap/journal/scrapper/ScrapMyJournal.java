package com.scrap.journal.scrapper;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ScrapMyJournal {

	public static final Logger logger = LogManager.getLogger(ScrapMyJournal.class);

	private static String JOURNAL_CONFIG = "src/main/resources/journalConfig.json";
	Properties props = new Properties();
	private WebDriver driver;
	public static JSONObject jsonObject;

	void loadConfig() {
		try {
			// Load credentials and URL from JSON file
			JSONParser parser = new JSONParser();
			jsonObject = (JSONObject) parser.parse(new FileReader(JOURNAL_CONFIG));
			logger.info("Got Login json file, loading values..");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load configuration", e);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		ScrapMyJournal s = new ScrapMyJournal();
		s.init();
		Map<String, Object> configs = (Map<String, Object>) jsonObject.get("jour3");
		Map<String, Object> scrapingConfig = (Map<String, Object>) configs.get("scraping_config");
		String url = (String) configs.get("url");
		String doiPath = (String) scrapingConfig.get("doi_selector");
		String titlePathString = (String) scrapingConfig.get("title_selector");
		String abstractPath = (String) scrapingConfig.get("abstract_selector");
		String authorsPath = (String) scrapingConfig.get("authors_selector");
		String searchPath = (String) scrapingConfig.get("search_input_selector");
		String resultsPath = (String) scrapingConfig.get("results_selector");

		s.openJournal(url, false, null, null);
		Thread.sleep(3000);
		s.searchProduct(searchPath, "Fever");
		Thread.sleep(3000);
		List<String> results = s.extractSearchResults(resultsPath, 20, 1);
		logger.info(results);
		logger.info("Got {} results ", results.size());
		s.extractInfoFromResults(results, titlePathString, abstractPath, authorsPath, doiPath);

		// s.saudijournalOfAnesthesia();
	}

	public void init() {

		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		loadConfig();
	}

	public void openJournal(String url, boolean login, String username, String password) {
		driver.get(url);
//		driver.findElement(By.xpath("/html/body/div[5]/div[2]/div/div/div[2]/div/div/button[2]")).click();
		if (login) {
			// TODO : need to configure logi steps
		}
	}

	public void searchProduct(String searchConfig, String prodect) {
		String[] search = searchConfig.split(" ");
		if (search[0].equalsIgnoreCase("id")) {
			driver.findElement(By.id(search[1])).sendKeys(prodect + Keys.ENTER);

		} else {
			driver.findElement(By.xpath(search[1])).sendKeys(prodect + Keys.ENTER);

		}

	}

	public List<String> extractSearchResults(String listingXPath, int totalResults, int increasePattern) {
		List<String> results = new ArrayList<>();

		if (totalResults > 10)
			totalResults = 10;
		int j = 1;
		for (int i = 0; i < totalResults; i++) {
			String link = driver.findElement(By.xpath(listingXPath.replace("${index}", String.valueOf(j))))
					.getAttribute("href");
			results.add(link);
			j = j + increasePattern;
		}

		return results;
	}

	public void extractInfoFromResults(List<String> results, String titleConfig, String abstractConfig,
			String authorsConfig, String doiConfig) throws InterruptedException {
		String[] titleConfigAr = titleConfig.split(" ");
		String[] abstractConfigAr = abstractConfig.split(" ");
		String[] authorsConfigAr = authorsConfig.split(" ");
		String[] doiConfigAr = doiConfig.split(" ");

		for (String result : results) {
			driver.get(result);
			Thread.sleep(5000);
			if (doiConfigAr[0].equalsIgnoreCase("id")) {
				try {
					String doi = driver.findElement(By.id(doiConfigAr[1])).getText();

//					Matcher matcherForDoi = Pattern.compile("DOI:\\s*(\\S+)").matcher(doi);
//
//					if (matcherForDoi.find()) {
//						doi = matcherForDoi.group(1);
//					} else {
//						logger.error("DOI not found.");
//					}
					logger.info("Doi : " + doi);
				} catch (Exception e) {

				}
			} else {
				try {
					String doi = driver.findElement(By.xpath(doiConfigAr[1])).getText();
					logger.info("Doi : " + doi);

				} catch (Exception e) {
					logger.error("Doi Not Found");
				}
			}
			try {
				String title;
				if (titleConfigAr[0].equalsIgnoreCase("id")) 
					title = driver.findElement(By.id(titleConfigAr[1])).getText();
				else 
					title = driver.findElement(By.xpath(titleConfigAr[1])).getText();
				logger.info("Title : " + title);

			} catch (Exception e) {
				logger.error("Title not found.");
			}
			try {
				String abstractText;
				if (abstractConfigAr[0].equalsIgnoreCase("id")) 
					abstractText = driver.findElement(By.id(abstractConfigAr[1])).getText();
				else 
					abstractText = driver.findElement(By.xpath(abstractConfigAr[1])).getText();

				logger.info("Abstract : " + abstractText);

			} catch (Exception e) {
				logger.error("Abstract not found.");
			}
			try {
				String authors;
				if (authorsConfigAr[0].equalsIgnoreCase("id")) 
					authors = driver.findElement(By.id(authorsConfigAr[1])).getText();
				else
					authors = driver.findElement(By.xpath(authorsConfigAr[1])).getText();
				
				logger.info("Authors : " + authors);
			} catch (Exception e) {
				logger.error("Authors not found.");
			}
			logger.info(
					"___________________________________________________________________________________________________________________________________");
		}
	}

}
