package com.journal.scrap.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.journal.scrap.dao.JournalApiService;
import com.journal.scrap.model.LocalLitAlertItemModel;
import com.journal.scrap.model.WebScrapingReqModel;
import com.journal.scrap.model.WebScrapingRespModel;
import com.journal.scrap.util.ScrapperUtil;

@Service
public class ScrapRestService extends ScrapperUtil {
	public static final Logger logger = LogManager.getLogger(ScrapRestService.class);

	@Autowired
	private JournalApiService crudService;
	

	public WebScrapingRespModel startScraping(WebScrapingReqModel requestModel) throws InterruptedException {
		init();
		JSONObject journalConfig = getJsonObject(requestModel.getJournalConfig());
		List<String> products = requestModel.getProducts();
		Map<String, String> loginCredential = requestModel.getCredentials();
		UUID parentId = requestModel.getAlertModel().getId();
		
		WebScrapingRespModel resp = new WebScrapingRespModel();

		
		JSONObject scrapingConfig = (JSONObject) journalConfig.get(SCRAPING_CONFIF);
		JSONObject filterConfig = (JSONObject) journalConfig.get(FILTER_CONFIG);
		
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

		// searching for each product in given journal
		for (String product : products) {
			try {
				try {
					login = openJournal(url, login, journalConfig, loginCredential);
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
				List<LocalLitAlertItemModel> litAlertModelList = extractKitAlertFromResults(results, scrapingConfig, parentId, product);

				resp.setAlertModel(requestModel.getAlertModel());
				resp.setListArticles(litAlertModelList);
//				litAlertModelList.stream().forEach(litAlertModel -> litAlertModel.getProducts().add(product));
//				crudService.saveArticles(articles);
//					prod.setArticles(articles);
			} catch (Exception e) {
				logger.error("Got error while scaraping product {} on {}, {}", product, url, e.getMessage());
			}
		}
		if (driver != null) {
			driver.quit();
			logger.info("driver Quit");
		}
		logger.info("Script complete!");
		return resp;
	}



	public boolean openJournal(String url, boolean login, JSONObject journalConfig, Map<String, String> loginCredential) {
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

	public List<LocalLitAlertItemModel> extractKitAlertFromResults(List<String> articleUrlList, Map<String, Object> scrapingConfig, UUID parentId, String product) throws InterruptedException {
		List<LocalLitAlertItemModel> litAlertModelList = new ArrayList<>();
		String doiConfig = (String) scrapingConfig.get(DOI_SELECTOR);
		String titleConfig = (String) scrapingConfig.get(TITLE_SELECTOR);
		String abstractConfig = (String) scrapingConfig.get(ABSTRACT_SELECTOR);
		String authorsConfig = (String) scrapingConfig.get(AUTHORS_SELECTOR);
		boolean extractDoi = (boolean) scrapingConfig.get(EXTRACT_DOI);

		for (String articleUrl : articleUrlList) {
			driver.get(articleUrl);
			logger.info("Scrapping the article {}", articleUrl);

			LocalLitAlertItemModel litAlertModel = new LocalLitAlertItemModel();
			litAlertModel.setParentId(parentId);
			litAlertModel.getProducts().add(product);
			litAlertModel.setLink(articleUrl);
			Thread.sleep(deley);
			try {
				String doi = getText(doiConfig);
				if (extractDoi)
					doi = extractDoi(doi);
				logger.info("Doi : " + doi);
				litAlertModel.setDoi(doi);
			} catch (Exception e) {
				logger.info("Doi Not Found");
			}

			try {
				String title = getText(titleConfig);
				logger.info("Title : " + title);
				litAlertModel.setTitle(title);
			} catch (Exception e) {
				logger.error("Title not found.");
			}
			try {
				String abstractText = getText(abstractConfig);
				logger.info("Abstract : " + abstractText);
				litAlertModel.setAbsCitation(abstractText);
			} catch (Exception e) {
				logger.error("Abstract not found.");
			}
			try {
				String authors = getText(authorsConfig);
				logger.info("Authors : " + authors);
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

	private JSONObject getJsonObject(String jsonString) {
		JSONParser parser = new JSONParser();
		try {
			return (JSONObject) parser.parse(jsonString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
