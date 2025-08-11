package com.journal.scrap.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.springframework.stereotype.Component;

import com.journal.scrap.entities.Article;
import com.journal.scrap.entities.Journal;

import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public abstract class ScrapperUtil implements ScrapperConfigKeys {
	private static final Logger logger = LogManager.getLogger(ScrapperUtil.class);

	public static int deley = 3000;
	public static int startingIndex = 1;
	
	protected WebDriver driver;
	public static JSONObject jsonObject;
	protected static String configDirectory;

	public void applyFilters(JSONObject filterConfig) throws InterruptedException {
		logger.info("Inside filter");
		String selectButtonConfig = (String) filterConfig.get(SELECT_BUTTON);
		String selectOptionConfig = (String) filterConfig.get(SELECT_OPTION);
		
		selectorClick(selectButtonConfig);
		Thread.sleep(deley);
		selectorClick(selectOptionConfig);
		Thread.sleep(deley);
	}

	public void init() {
		ChromeOptions options = new ChromeOptions();
//		options.addArguments("--headless=new");  // or "--headless" for older versions
		options.addArguments("--disable-gpu"); // Optional: better compatibility
//		options.addArguments("--window-size=1920,1080");  
		driver = new ChromeDriver(options);
//		driver = new ChromeDriver();
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

	public String extractDoi(String value) {
		Matcher matcherForDoi = Pattern.compile(DOI_REGEX).matcher(value);

		if (matcherForDoi.find()) {
			return matcherForDoi.group(1);
		} else {
			logger.error("DOI not found.");
		}
		return "NA";
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

}
