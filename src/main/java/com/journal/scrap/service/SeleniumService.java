package com.journal.scrap.service;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.journal.scrap.dao.JournalApiService;
import com.journal.scrap.entities.Article;
import com.journal.scrap.entities.Journal;
import com.journal.scrap.entities.Product;
import com.journal.scrap.util.ScrapperConfigKeys;
import com.journal.scrap.util.ScrapperUtil;

import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public class SeleniumService extends ScrapperUtil{

	public static final Logger logger = LogManager.getLogger(SeleniumService.class);

	@Autowired
	private JournalApiService crudService;


	public static void main(String[] args) throws InterruptedException {
		SeleniumService s = new SeleniumService();
		s.startScraping();
	}

	private JSONObject loadConfig(String ConfigFilePath) {
		try {
			// Load credentials and URL from JSON file
			JSONParser parser = new JSONParser();
			jsonObject = (JSONObject) parser.parse(new FileReader(ConfigFilePath));
			logger.info("Got Login json file, loading values..");
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load configuration ", e);
		}
	}

	public void startScraping() throws InterruptedException {
		init();
		for (String configFilePath : loadConfigFiles()) {
			// loading configurations
			JSONObject journalConfig = loadConfig(configFilePath);
			JSONObject scrapingConfig = (JSONObject) journalConfig.get(SCRAPING_CONFIF);
			JSONObject filterConfig = (JSONObject) journalConfig.get(FILTER_CONFIG);
			List<String> products = (List<String>) journalConfig.get(PRODUCTS);

			boolean login = (boolean) journalConfig.get(LOGIN);
			boolean applyFiltes = (boolean) journalConfig.get(APPLY_FILTER);

			int increaseInListPage = ((Long) scrapingConfig.get(INCREASE_PATTERN_IN_LIST_PAGE)).intValue();
			try {
				startingIndex = ((Long) scrapingConfig.get(STARTING_INDEX_FOR_LIST_PAGE)).intValue();
				deley = ((Long) scrapingConfig.get(DELEY)).intValue();
			} catch (Exception e) {
				logger.error("value is not specified in config usin default value {}", e.getMessage());
			}
			String url = (String) journalConfig.get(URL);
			String name = (String) journalConfig.get(JOURNAL_NAME);
			logger.info("Starting for {}", url);
			Thread.sleep(deley);

			Journal journal = new Journal();
			journal.setUrl(url);
			journal.setName(name);

			// searching for each product in given journal
			for (String product : products) {
				try {
					try {
						login = openJournal(url, login, journalConfig);
					} catch (Exception e) {
						logger.error("Got error while login {}", e.getMessage());
					}
					String searchPath = (String) scrapingConfig.get(SEARCH_INPUT_SELECTOR);
					String resultsPath = (String) scrapingConfig.get(RESULT_SELECTOR);

					Thread.sleep(deley);
					searchProduct(searchPath, product);
					Thread.sleep(deley);
					// Apply filters
					if (applyFiltes) {
						applyFilters(filterConfig);
					}
					Thread.sleep(deley);
					List<String> results = extractSearchResults(resultsPath, 100, startingIndex, increaseInListPage);
					logger.info(results);
					logger.info("Got {} results ", results.size());
					List<Article> articles = extractInfoFromResults(results, scrapingConfig, journal);

					articles.stream().forEach(article -> article.setProduct(product));
					crudService.saveArticles(articles);
//					prod.setArticles(articles);
				} catch (Exception e) {
					logger.error("Got error while scaraping product {} on {}, {}", product, url, e.getMessage());
				}
			}
		}
		if (driver != null) {
			driver.quit();
			logger.info("driver Quit");
		}
		logger.info("Script complete!");
	}


	public boolean openJournal(String url, boolean login, JSONObject journalConfig) {
		try {
			driver.get(url);
		} catch (Exception e) {
			logger.info("Something wrong with the website {}", url);
			logger.error(e.getMessage());
		}
		try {
			selectorClick((String) journalConfig.get(COOKIE_SELECTOR));
		} catch (Exception e) {
			logger.info("Config for Cookie popup not found or not configured");
		}
		if (login) {
			login(journalConfig);
			login = false;
		}
		return login;
	}


	private void login(JSONObject journalConfig) {
		JSONObject loginConfig = (JSONObject) journalConfig.get(LOGIN_CONFIG);
		String loginForm = ((String) loginConfig.get(LOGIN_SELECTOR));
		String userIdSelector = ((String) loginConfig.get(USERNAME_SELECTOR));
		String paswordSelector = ((String) loginConfig.get(PASSWORD_SELECTOR));
		String SubmitButtonSelector = ((String) loginConfig.get(SUBMIT_BTN_SELECTOR));
		String userId = (String) loginConfig.get(USER_ID);
		String password = (String) loginConfig.get(PASSWORD);

		try {
			logger.info("Inside login");
			Thread.sleep(deley);
			selectorClick(loginForm);
			Thread.sleep(deley);
			selectorInput(userIdSelector, userId);
			Thread.sleep(deley);
			selectorInput(paswordSelector, password);
			Thread.sleep(deley);
			selectorClick(SubmitButtonSelector);
		} catch (InterruptedException e) {
			logger.error("got error while login");
			e.printStackTrace();
		}
	}

	public List<String> loadConfigFiles() {

		List<String> files = new ArrayList<>();
		Path directoryPath = Paths.get(configDirectory);

		try (Stream<Path> paths = Files.walk(directoryPath)) {
			files = paths.filter(Files::isRegularFile).map(path -> configDirectory + path.getFileName().toString())
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}
}
