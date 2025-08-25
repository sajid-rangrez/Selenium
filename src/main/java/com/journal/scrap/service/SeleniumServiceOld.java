package com.journal.scrap.service;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.journal.scrap.dao.JournalApiService;
import com.journal.scrap.entities.Article;
import com.journal.scrap.entities.Journal;
import com.journal.scrap.util.ScrapperUtil;

import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public class SeleniumServiceOld extends ScrapperUtil{

	public static final Logger logger = LogManager.getLogger(SeleniumServiceOld.class);
	

	protected static String configDirectory;

	@Autowired
	private JournalApiService crudService;


	public static void main(String[] args) throws InterruptedException {
		String text = "Showing 75 results for";
        Pattern pattern = Pattern.compile("Showing\\s+(\\d+)\\s+results\\s+for");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            System.out.println(matcher.group(1)); // prints: 6 of 6 items
        }
//		articleCount();
	}
//	public void articleCount(JSONObject articleConfig) throws InterruptedException {
//		try {
//
//			String regexConfig = (String) articleConfig.get(ARTICLE_COUNT_REGEX);
//			String selectArticleCount = (String) articleConfig.get(ARTICLE_COUNT_SIZE);
//			String text = driver.findElement(By.xpath(selectArticleCount)).getText();
// 
//			Pattern pattern = Pattern.compile(regexConfig);
//	        Matcher matcher = pattern.matcher(text);
//	        
//	        if (matcher.find()) {
//	            System.out.println(matcher.group(1)); // prints: 6 of 6 items
//	        }
//			
//		} catch (Exception e) {
//			logger.error("Error while fetching Article Count text", e);
//		}
// 
//	}
	
	public static void articleCount() throws InterruptedException {
		try {
//			String selectArticleCount = (String) articleConfig.get(ARTICLE_COUNT_SIZE);
//			String text = driver.findElement(By.xpath(selectArticleCount)).getText();
//			String count = text.replaceAll(".*\\b(\\d+)\\s*items$", "$1");
//			logger.info("Article Count: {}", count);
 
			String regexConfig = ".*of\\s+(\\d+)\\s+items$->$1";
 
			// Split regex and replacement using "->"
			String[] parts = regexConfig.split("->");
			String regexPattern = parts[0];
			String replacement = parts[1];
 
			// Get article text from the page
//			String selectArticleCount = (String) articleConfig.get(ARTICLE_COUNT_SIZE);
			String text = "1 - 6 of 6 items";
 
			// Apply regex dynamically
			String count = text.replaceAll(regexPattern, replacement);
 
			// Log result
			logger.info("Article Count: {}", count);
		} catch (Exception e) {
			logger.error("Error while fetching Article Count text", e);
		}
 
	}
	
	public void init() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");  // or "--headless" for older versions
		options.addArguments("--disable-gpu"); // Optional: better compatibility
		options.addArguments("--window-size=1920,1080");  
		driver = new ChromeDriver(options);
		WebDriverManager.chromedriver().setup();
		driver.manage().window().maximize();
		Properties prop = new Properties();

		if (configDirectory == null) {
			try {
				prop.load(new FileInputStream(ENV_PROPERTIES));
				configDirectory = prop.getProperty(CONFIG_DIRECTORY);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
//					crudService.saveArticles(articles);
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
	public List<String> extractSearchResults(String listingXPath, int totalResults, int startingIndex,
			int increasePattern) {
		List<String> results = new ArrayList<>();

		int j = startingIndex;
		for (int i = 0; i < totalResults; i++) {
			try {
				String link = driver.findElement(By.xpath(listingXPath.replace("${index}", String.valueOf(j))))
						.getAttribute(LINK_ATTRIBUTE);
				results.add(link);
				j = j + increasePattern;
			} catch (Exception e) {
				logger.error("Can't find element on index {}.", i);
				break;
			}
		}
		return results;
	}
	public List<Article> extractInfoFromResults(List<String> articleList, Map<String, Object> scrapingConfig,
			Journal journal) throws InterruptedException {
		List<Article> articles = new ArrayList<>();
		String doiConfig = (String) scrapingConfig.get(DOI_SELECTOR);
		String titleConfig = (String) scrapingConfig.get(TITLE_SELECTOR);
		String abstractConfig = (String) scrapingConfig.get(ABSTRACT_SELECTOR);
		String authorsConfig = (String) scrapingConfig.get(AUTHORS_SELECTOR);
		boolean extractDoi = (boolean) scrapingConfig.get(EXTRACT_DOI);

		for (String result : articleList) {
			driver.get(result);
			logger.info("Scrapping the article {}", result);

			Article article = new Article();
			article.setLink(result);
			Thread.sleep(deley);
			try {
				String doi = getText(doiConfig);
				if (extractDoi)
					doi = extractDoi(doi);
				logger.info("Doi : " + doi);
				article.setDoi(doi);
			} catch (Exception e) {
				logger.info("Doi Not Found");
			}

			try {
				String title = getText(titleConfig);
				logger.info("Title : " + title);
				article.setTitle(title);
			} catch (Exception e) {
				logger.error("Title not found.");
			}
			try {
				String abstractText = getText(abstractConfig);
				logger.info("Abstract : " + abstractText);
				article.setAbs(abstractText);
			} catch (Exception e) {
				logger.error("Abstract not found.");
			}
			try {
				String authors = getText(authorsConfig);
				logger.info("Authors : " + authors);
				article.setAuthors(authors);
			} catch (Exception e) {
				logger.error("Authors not found.");
			}
			articles.add(article);
			logger.info(
					"___________________________________________________________________________________________________________________________________");
		}
		return articles;
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
