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
import org.openqa.selenium.chromium.ChromiumDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ScrapMyJournal {

	public static final Logger logger = LogManager.getLogger(ScrapMyJournal.class);

	public static final String XPATH = "xpath";
	public static final String ID = "id";
	public static final String CSS = "css";
	public static final String CLASS = "class";
	private static final String JOURNAL_CONFIG = "src/main/resources/journalConfig.json";
	
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
		Map<String, Object> configs = (Map<String, Object>) jsonObject;
//		for (Map.Entry<String, Object> entry : configs.entrySet()) {
//			Map<String, Object> journalConfig = (Map<String, Object>) entry.getValue();
			Map<String, Object> journalConfig = (Map<String, Object>) configs.get("jour2");
			Map<String, Object> scrapingConfig = (Map<String, Object>) journalConfig.get("scraping_config");
			int increaseInListPage = ((Long) scrapingConfig.get("increase_pattern_in_list_page")).intValue();
			String url = (String) journalConfig.get("url");
			logger.info("Starting for {}", url);
			List<String> products = (List<String>) journalConfig.get("products");

			Thread.sleep(2000);
			boolean login = (boolean) journalConfig.get("login");
			s.openJournal(url, login, journalConfig);
			for (String product : products) {
				String searchPath = (String) scrapingConfig.get("search_input_selector");
				String resultsPath = (String) scrapingConfig.get("results_selector");

				Thread.sleep(2000);
				s.searchProduct(searchPath, product);
				Thread.sleep(3000);
				List<String> results = s.extractSearchResults(resultsPath, 200, increaseInListPage);
				logger.info(results);
				logger.info("Got {} results ", results.size());
				s.extractInfoFromResults(results, scrapingConfig);

				// s.saudijournalOfAnesthesia();
//			}
		}
	}

	public void init() {

		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		loadConfig();
	}

	public void openJournal(String url, boolean login, Map<String, Object> journalConfig) {
		driver.get(url);
//		driver.findElement(By.xpath("/html/body/div[5]/div[2]/div/div/div[2]/div/div/button[2]")).click();
		if (login) {
			Map<String, Object> loginConfig = (Map<String, Object>) journalConfig.get("login_config");
			login(loginConfig);
		}
	}

	public List<String> extractSearchResults(String listingXPath, int totalResults, int increasePattern) {
		List<String> results = new ArrayList<>();

		driver.findElements(By.id("#checkBoxListContainer"));
		int j = 1;
		for (int i = 0; i < totalResults; i++) {
			try {
				String link = driver.findElement(By.xpath(listingXPath.replace("${index}", String.valueOf(j))))
						.getAttribute("href");
				results.add(link);
				j = j + increasePattern;
			} catch (Exception e) {
				break;
			}
		}
		return results;
	}

	public void extractInfoFromResults(List<String> results, Map<String, Object> scrapingConfig)
			throws InterruptedException {
		String doiConfig = (String) scrapingConfig.get("doi_selector");
		String titleConfig = (String) scrapingConfig.get("title_selector");
		String abstractConfig = (String) scrapingConfig.get("abstract_selector");
		String authorsConfig = (String) scrapingConfig.get("authors_selector");
		boolean extractDoi = (boolean) scrapingConfig.get("extract_doi_from_text");

		for (String result : results) {
			driver.get(result);
			Thread.sleep(5000);

			try {
				String doi = getText(doiConfig);
				if (extractDoi)
					doi = extractDoi(doi);
				logger.info("Doi : " + doi);

			} catch (Exception e) {
				logger.info("Doi Not Found");
			}

			try {
				String title = getText(titleConfig);
				logger.info("Title : " + title);

			} catch (Exception e) {
				logger.error("Title not found.");
			}
			try {
				String abstractText = getText(abstractConfig);
				logger.info("Abstract : " + abstractText);

			} catch (Exception e) {
				logger.error("Abstract not found.");
			}
			try {
				String authors = getText(authorsConfig);
				logger.info("Authors : " + authors);
			} catch (Exception e) {
				logger.error("Authors not found.");
			}
			logger.info(
					"___________________________________________________________________________________________________________________________________");
		}
	}

	public String extractDoi(String value) {
		Matcher matcherForDoi = Pattern.compile("DOI:\\s*(\\S+)").matcher(value);

		if (matcherForDoi.find()) {
			return matcherForDoi.group(1);
		} else {
			logger.error("DOI not found.");
		}
		return "NA";
	}

	private void login(Map<String, Object> loginConfig) {
		String loginForm = ((String) loginConfig.get("login_form_selector"));
		String userIdSelector = ((String) loginConfig.get("username_selector"));
		String paswordSelector = ((String) loginConfig.get("password_selector"));
		String SubmitButtonSelector = ((String) loginConfig.get("submit_button_selector"));
		String userId = (String) loginConfig.get("username-email");
		String password = (String) loginConfig.get("password");

		try {
			Thread.sleep(3000);
			selectorClick(loginForm);
			selectorInput(userIdSelector, userId);
			Thread.sleep(1000);
			selectorInput(paswordSelector, password);
			Thread.sleep(3000);
			selectorClick(SubmitButtonSelector);

			logger.info("inside login");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void selectorInput(String value, String text) {
		String[] selector = value.split(" ");
		if (selector[0].equals(ID))
			driver.findElement(By.id(selector[1])).sendKeys(text);
		else if (selector[0].equals(CSS))
			driver.findElement(By.cssSelector(selector[1])).sendKeys(text);
		else
			driver.findElement(By.xpath(selector[1])).sendKeys(text);

	}

	public void searchProduct(String searchConfig, String prodect) {
		String[] search = searchConfig.split(" ");
		logger.info("inside SearchProduct : {}",prodect);
		
		if (search[0].equalsIgnoreCase(ID)) {
			driver.findElement(By.id(search[1])).sendKeys(prodect + Keys.ENTER);
			logger.info("Seatching for product : {} using id",prodect);
		} else if (search[0].equalsIgnoreCase(XPATH)) {
			driver.findElement(By.xpath(search[1])).sendKeys(prodect + Keys.ENTER);
			logger.info("Seatching for product : {} using xpath",prodect);

		} else if (search[0].equalsIgnoreCase(CSS)) {
			driver.findElement(By.cssSelector(search[1])).sendKeys(prodect + Keys.ENTER);
			logger.info("Seatching for product : {} using css",prodect);
		} else if(search[0].equalsIgnoreCase(CLASS)) {
			driver.findElement(By.className(search[1])).sendKeys(prodect + Keys.ENTER);
			logger.info("Seatching for product : {} using className",prodect);
			
		}

	}

	public String getText(String value) {
		String[] selector = value.split(" ");
		if (selector[0].equals(ID))
			return driver.findElement(By.id(selector[1])).getText();
		else if (selector[0].equals(CSS))
			return driver.findElement(By.cssSelector(selector[1])).getText();
		else
			return driver.findElement(By.xpath(selector[1])).getText();

	}

	public void selectorClick(String value) {
		String[] selector = value.split(" ");
		try {
			if (selector[0].equals(ID)) {
				driver.findElement(By.id(selector[1])).click();
				logger.info("inside try for id");
			} else if (selector[0].equals(CSS)) {
				driver.findElement(By.cssSelector(selector[1])).click();
				logger.info("inside try for css");
			} else if (selector[0].equals(CLASS)) {
				driver.findElement(By.className(selector[1])).click();
				logger.info("inside try for class");
			} else {
				driver.findElement(By.xpath(selector[1])).click();
				logger.info("inside try for xpath");
			}
		} catch (Exception e) {
			if (selector[0].equals(ID)) {
				((ChromiumDriver) driver).executeScript("arguments[0].click();",
						driver.findElement(By.id(selector[1])));
				logger.info("inside catch for id");
			} else if (selector[0].equals(CSS)) {
				((ChromiumDriver) driver).executeScript("arguments[0].click();",
						driver.findElement(By.cssSelector(selector[1])));
				logger.info("inside catch for css");
			} else {
				((ChromiumDriver) driver).executeScript("arguments[0].click();",
						driver.findElement(By.xpath(selector[1])));
				logger.info("inside catch for xpath");
			}
		}

	}

}
