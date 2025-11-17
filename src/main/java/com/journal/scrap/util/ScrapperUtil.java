package com.journal.scrap.util;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.springframework.stereotype.Component;

import com.journal.scrap.model.LocalLitAlertItemModel;

import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public class ScrapperUtil implements ScrapperConfigKeys {
	private static final Logger logger = LogManager.getLogger(ScrapperUtil.class);

	public static int deley = 3000;
	public int startingIndex = 1;
	public boolean pagination = false;

	protected WebDriver driver;
	public static JSONObject jsonObject;
	
	public static String CURRENT_WORKING_DIR;
	public static String EVIDENCE_DIR;

	public void applyFilters(JSONObject filterConfig) throws InterruptedException {
		logger.info("Inside filter");
		String selectButtonConfig = (String) filterConfig.get(SELECT_BUTTON);
		String selectOptionConfig = (String) filterConfig.get(SELECT_OPTION);

		selectorClick(selectButtonConfig);
		deley();
		selectorClick(selectOptionConfig);
		deley();
	}
	public String articleCount(JSONObject articleConfig) throws InterruptedException {
		try {
 
			String regexPattern = (String) articleConfig.get(ARTICLE_COUNT_REGEX);
			String selectArticleCount = (String) articleConfig.get(ARTICLE_COUNT_SIZE);
			// String text = driver.findElement(By.xpath(selectArticleCount)).getText();
			String text = getText(selectArticleCount);
 
			Matcher matcher = Pattern.compile(regexPattern)
					.matcher(text);
			
			
			String totalArticles = null;
			if (matcher.find()) {
				totalArticles = matcher.group(1); // str to intt
				logger.info("Found {} Articles on search", totalArticles);
			} else {
				logger.info("Unable to find ");
			}
			return totalArticles;
		} catch (Exception e) {
			logger.error("Error while fetching Article Count text", e);
			return null;
		}
 
	}

	public void init() {
//		ChromeOptions options = new ChromeOptions();
//		options.addArguments("--headless=new"); // or "--headless" for older versions
//		options.addArguments("--disable-gpu"); // Optional: better compatibility
//		options.addArguments("--window-size=1920,1080");
//		options.addArguments("--start-maximized");
//
//		driver = new ChromeDriver(options);
//		WebDriverManager.chromedriver().setup();
//		driver.manage().window().maximize();
		
		WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // use "--headless" if "--headless=new" gives issues
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080"); // full HD default
        this.driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1920, 1080));	
		
		initEvidence();
	}

	public void initEvidence() {
		CURRENT_WORKING_DIR = System.getProperty("user.dir");
		System.out.println("currentWorkingDir: " + CURRENT_WORKING_DIR);
		EVIDENCE_DIR = CURRENT_WORKING_DIR + "\\Evidences";

		File theDir = new File(EVIDENCE_DIR);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
	}

	public boolean openJournal(String url, boolean login, JSONObject journalConfig,
			Map<String, String> loginCredential) {
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
			login(journalConfig, loginCredential);
			login = false;
		}
		return login;
	}

	private void login(JSONObject journalConfig, Map<String, String> loginCredential) {
		JSONObject loginConfig = (JSONObject) journalConfig.get(LOGIN_CONFIG);
		String loginForm = ((String) loginConfig.get(LOGIN_SELECTOR));
		String userIdSelector = ((String) loginConfig.get(USERNAME_SELECTOR));
		String paswordSelector = ((String) loginConfig.get(PASSWORD_SELECTOR));
		String SubmitButtonSelector = ((String) loginConfig.get(SUBMIT_BTN_SELECTOR));
		String userId = loginCredential.get(USER_ID);
		String password = loginCredential.get(PASSWORD);

		try {
			logger.info("Inside login");
			deley();
			selectorClick(loginForm);
			deley();
			selectorInput(userIdSelector, userId);
			deley();
			selectorInput(paswordSelector, password);
			deley();
			selectorClick(SubmitButtonSelector);
		} catch (InterruptedException e) {
			logger.error("got error while login");
			e.printStackTrace();
		}
	}

	public List<String> extractSearchResults(JSONObject scrapingConfig, int totalResults) throws InterruptedException {
		try {
			startingIndex = ((Long) scrapingConfig.get(STARTING_INDEX_FOR_LIST_PAGE)).intValue();
		} catch (Exception e) {
			logger.warn("starting index is not specified in config using default value 1.");
		}
		try {
			pagination = (boolean) scrapingConfig.get(ENABLE_PAGINATION);
		} catch (NullPointerException npe) {
			logger.warn("Pagination flag is not present in configuration");
		}
		String listingXPath = (String) scrapingConfig.get(RESULT_SELECTOR);
		String nextPageSelector = (String) scrapingConfig.get(NEXT_PAGE_BUTTON);
		int increasePattern = ((Long) scrapingConfig.get(INCREASE_PATTERN_IN_LIST_PAGE)).intValue();

		List<String> results = new ArrayList<>();

		int page = 1;
		int listingIndex = startingIndex;
		for (int i = 0; i < totalResults; i++) {
			try {
				String link = driver
						.findElement(By.xpath(listingXPath.replace("${index}", String.valueOf(listingIndex))))
						.getAttribute(LINK_ATTRIBUTE);
				results.add(link);
				listingIndex = listingIndex + increasePattern;
			} catch (Exception e) {
				if (pagination) {
					deley();
					try {
						selectorClick(nextPageSelector);
						listingIndex = startingIndex;
						i = 0;
						page++;
						logger.info("navigating to page no {}", page);
						deley();
					} catch (Exception e2) {
						logger.error("Next Page navagation button not found!");
						break;
					}
				} else {
					logger.error("Can't find element on index {}.", i);
					break;
				}
			}
		}
		return results;
	}

	public List<LocalLitAlertItemModel> extractLitAlertFromResults(List<String> articleUrlList,
			Map<String, Object> scrapingConfig, UUID parentId, String product) throws InterruptedException {
		List<LocalLitAlertItemModel> litAlertModelList = new ArrayList<>();
		String doiConfig = (String) scrapingConfig.get(DOI_SELECTOR);
		String titleConfig = (String) scrapingConfig.get(TITLE_SELECTOR);
		String abstractConfig = (String) scrapingConfig.get(ABSTRACT_SELECTOR);
		String articleBodyConfig = (String) scrapingConfig.get(ARTICLE_BODY_SELECTOR);
		String authorsConfig = (String) scrapingConfig.get(AUTHORS_SELECTOR);
		boolean extractDoi = (boolean) scrapingConfig.get(EXTRACT_DOI);

		int i = 0;
		for (String articleUrl : articleUrlList) {
			driver.get(articleUrl);
			logger.info("Scrapping the article {}", articleUrl);
			if(i++ > 10) {
				break;
			}

			LocalLitAlertItemModel litAlertModel = new LocalLitAlertItemModel();
			litAlertModel.setParentId(parentId);
			litAlertModel.getProducts().add(product);
			litAlertModel.setSource(articleUrl);
			deley();
			try {
				String doi = getText(doiConfig);
				if (extractDoi)
					doi = extractDoi(doi);
				logger.info("Doi : " + doi);
				litAlertModel.setDoi(doi);
				litAlertModel.setFtaLink(FTA_URL + doi);
			} catch (Exception e) {
				logger.info("Doi Not Found");
			}

			try {
				String title = getText(titleConfig);
//				logger.info("Title : " + title);
				litAlertModel.setTitle(title);
			} catch (Exception e) {
				logger.error("Title not found.");
			}

			try {
				String articleBody = getText(articleBodyConfig);
//				logger.info("Article Body : " + articleBody);
				litAlertModel.setArticleBody(articleBody);
			} catch (Exception e) {
				logger.error("Article Body not found.");
			}
			try {
				String abstractText = "";
				if (abstractConfig.contains(LIST_FLAG))
					abstractText = getTextMultiple(abstractConfig);
				else
					abstractText = getText(abstractConfig);
//				logger.info("Abstract : " + abstractText);
				litAlertModel.setAbsCitation(abstractText);
			} catch (Exception e) {
				logger.error("Abstract not found.");
			}
			try {
				String authors;
				if (abstractConfig.contains(LIST_FLAG))
					authors = getTextMultiple(authorsConfig);
				else
					authors = getText(authorsConfig);
//				logger.info("Authors : " + authors);
				litAlertModel.setAuthor(authors);
			} catch (Exception e) {
				logger.error("Authors not found.");
			}
			litAlertModelList.add(litAlertModel);
			logger.info(
					"___________________________________________________________________________________________________________________________________");
		}
		return litAlertModelList;
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
		String[] search = searchConfig.split(SPLIT_BY);
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
		String[] selector = value.split(SPLIT_BY);
		if (selector[0].equals(ID))
			return driver.findElement(By.id(selector[1])).getText();
		else if (selector[0].equals(CSS))
			return driver.findElement(By.cssSelector(selector[1])).getText();
		else
			return driver.findElement(By.xpath(selector[1])).getText();

	}

	public String getTextMultiple(String value) {
		// this method is for special cases like if there are multiple authors and all
		// are in diffrent blocks
		String[] selector = value.split(SPLIT_BY);
		String resValue = "";
		List<WebElement> webElements = null;

		if (selector[0].equals(ID))
			webElements = driver.findElements(By.id(selector[1]));
		else if (selector[0].equals(CSS))
			webElements = driver.findElements(By.cssSelector(selector[1]));
		else if (selector[0].equals(CLASS))
			webElements = driver.findElements(By.className(selector[1]));
		else
			webElements = driver.findElements(By.xpath(selector[1]));

		if (webElements != null) {
			for (WebElement element : webElements) {
				resValue += element.getText() + "; ";
			}
		}
		return resValue;
	}

	public List<String> getTextList(String value) {
		String[] selector = value.split(SPLIT_BY);
		List<WebElement> webElements = null;
		List<String> textValues = new ArrayList<>();

		try {
			if (selector[0].equals(CSS)) {
				webElements = driver.findElements(By.cssSelector(selector[1]));
			} else if (selector[0].equals(CLASS)) {
				webElements = driver.findElements(By.cssSelector(selector[1]));
			}
			for (WebElement element : webElements) {
				textValues.add(element.getText());
			}
		} catch (Exception e) {
			logger.error("Got error while extracting element List : {}", e.getMessage());
		}
		return textValues;
	}

	public void selectorClick(String value) throws InterruptedException {
		String[] selector = value.split(SPLIT_BY);
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
			WebElement element;
			if (selector[0].equals(ID))
				element = driver.findElement(By.id(selector[1]));
			else if (selector[0].equals(CSS))
				element = driver.findElement(By.cssSelector(selector[1]));
			else if (selector[0].equals(CLASS))
				element = driver.findElement(By.className(selector[1]));
			else
				element = driver.findElement(By.xpath(selector[1]));

			((ChromiumDriver) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();",
					element);

			logger.info("Clicking on {} using javaScript executer!", value);
		}
		deley();
	}

	public void deley() {
		try {
			Thread.sleep(deley);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
