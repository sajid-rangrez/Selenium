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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.journal.scrap.dao.JournalApiService;
import com.journal.scrap.entities.Article;
import com.journal.scrap.entities.Journal;
import com.journal.scrap.entities.Product;
import com.journal.scrap.scrapper.ScrapperConfigKeys;

import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public class SeleniumService implements ScrapperConfigKeys {

	public static final Logger logger = LogManager.getLogger(SeleniumService.class);
	
	@Autowired
	private JournalApiService crudService;
	

	public static int deley = 3000;
	public static int startingIndex = 1;

	private WebDriver driver;
	public static JSONObject jsonObject;
	private static String configDirectory;

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
					if(applyFiltes) {
						applyFilters(filterConfig);
					}
					Thread.sleep(deley);
					List<String> results = extractSearchResults(resultsPath, 100, startingIndex, increaseInListPage);
					logger.info(results);
					logger.info("Got {} results ", results.size());
					List<Article> articles = extractInfoFromResults(results, scrapingConfig, journal);
					
					Product prod = new Product();
					prod.setName(product);
					Long productId = crudService.saveProduct(prod);
					articles.stream()
					.forEach(article -> article.setProductId(productId));
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

	private void applyFilters(JSONObject filterConfig) {
		try {
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void init() {
		ChromeOptions options = new ChromeOptions();
//		options.addArguments("--headless=new");  // or "--headless" for older versions
		options.addArguments("--disable-gpu");   // Optional: better compatibility
//		options.addArguments("--window-size=1920,1080");  
		driver = new ChromeDriver(options);
//		driver = new ChromeDriver();
		WebDriverManager.chromedriver().setup();
		driver.manage().window().maximize();
        Properties prop = new Properties();
        
        if(configDirectory == null) {
        	try {
        		prop.load(new FileInputStream(ENV_PROPERTIES));
        		configDirectory = prop.getProperty(CONFIG_DIRECTORY);
        	} catch (IOException e) {
        		e.printStackTrace();
        	}	
        }
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

	public List<Article> extractInfoFromResults(List<String> articleList, Map<String, Object> scrapingConfig, Journal journal)
			throws InterruptedException {
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

	public String extractDoi(String value) {
		Matcher matcherForDoi = Pattern.compile(DOI_REGEX).matcher(value);

		if (matcherForDoi.find()) {
			return matcherForDoi.group(1);
		} else {
			logger.error("DOI not found.");
		}
		return "NA";
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

	public void selectorInput(String configValue, String text) {
		String[] selector = configValue.split(SPLIT_BY);
		if (selector[0].equals(ID))
			driver.findElement(By.id(selector[1])).sendKeys(text);
		else if (selector[0].equals(XPATH))
			driver.findElement(By.xpath(selector[1])).sendKeys(text);
		else if (selector[0].equals(CSS))
			driver.findElement(By.cssSelector(selector[1])).sendKeys(text);
		else
			driver.findElement(By.className(selector[1])).sendKeys(text);
	}

	public void searchProduct(String searchConfig, String prodect) throws InterruptedException {
		String[] search = searchConfig.split(" ");
		logger.info("inside SearchProduct : {}", prodect);
		if (search.length == 3) {
			if (search[0].equalsIgnoreCase(ID)) {
				selectorClick(search[0] + " " + search[1]);
				driver.findElement(By.id(search[2])).sendKeys(prodect + Keys.ENTER);
			} else if (search[0].equalsIgnoreCase(XPATH)) {
				selectorClick(search[0] + " " + search[1]);
				driver.findElement(By.xpath(search[2])).sendKeys(prodect + Keys.ENTER);

			} else if (search[0].equalsIgnoreCase(CSS)) {
				selectorClick(search[0] + " " + search[1]);
				driver.findElement(By.cssSelector(search[2])).sendKeys(prodect + Keys.ENTER);
			} else if (search[0].equalsIgnoreCase(CLASS)) {
				selectorClick(search[0] + " " + search[1]);
				driver.findElement(By.className(search[2])).sendKeys(prodect + Keys.ENTER);
			}
			logger.info("Searching for product : {} using {}", prodect, search[0]);
		} else {
			if (search[0].equalsIgnoreCase(ID)) {
				driver.findElement(By.id(search[1])).sendKeys(prodect + Keys.ENTER);
			} else if (search[0].equalsIgnoreCase(XPATH)) {
				driver.findElement(By.xpath(search[1])).sendKeys(prodect + Keys.ENTER);
			} else if (search[0].equalsIgnoreCase(CSS)) {
				driver.findElement(By.cssSelector(search[1])).sendKeys(prodect + Keys.ENTER);
			} else if (search[0].equalsIgnoreCase(CLASS)) {
				driver.findElement(By.className(search[1])).sendKeys(prodect + Keys.ENTER);
			}
			logger.info("Searching for product : {} using {}", prodect, search[0]);
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

	public void selectorClick(String value) throws InterruptedException {
		String[] selector = value.split(" ");
		try {
			if (selector[0].equals(ID)) 
				driver.findElement(By.id(selector[1])).click();
			else if (selector[0].equals(CSS)) 
				driver.findElement(By.cssSelector(selector[1])).click();
			else if (selector[0].equals(CLASS))
				driver.findElement(By.className(selector[1])).click();
			else 
				driver.findElement(By.xpath(selector[1])).click();
			
			logger.info("clicking on {}", value);
		} catch (Exception e) {
			if (selector[0].equals(ID)) {
				((ChromiumDriver) driver).executeScript("arguments[0].click();",
						driver.findElement(By.id(selector[1])));
			} else if (selector[0].equals(CSS)) {
				((ChromiumDriver) driver).executeScript("arguments[0].click();",
						driver.findElement(By.cssSelector(selector[1])));
			} else {
				((ChromiumDriver) driver).executeScript("arguments[0].click();",
						driver.findElement(By.xpath(selector[1])));
			}
			logger.info("Clicking on {} using java executer in catch block.", value);
		}
		Thread.sleep(deley);
	}
	public List<String> loadConfigFiles(){
		
		List<String> files = new ArrayList<>();
		Path directoryPath = Paths.get(configDirectory);

        try (Stream<Path> paths = Files.walk(directoryPath)) {
        	files = paths
                    .filter(Files::isRegularFile)
                    .map(path -> configDirectory+path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
	}
}
