package com.journal.scrap.service;

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
import com.journal.scrap.model.WebScrapingRequest;
import com.journal.scrap.model.WebScrapingResponse;
import com.journal.scrap.util.ScrapperUtil;

@Service
public class ScrapRestService extends ScrapperUtil {
	public static final Logger logger = LogManager.getLogger(ScrapRestService.class);

	@Autowired
	private JournalApiService crudService;

	public String startThread(WebScrapingRequest requestModel) {
		ScrapRestService newThread = new ScrapRestService();
		Thread thread = new Thread(() -> newThread.startScraping(requestModel));
		thread.start();
		return "Scraping Initiated";
	}

	public WebScrapingResponse startScraping(WebScrapingRequest requestModel) {
		init();
		JSONObject journalConfig = getJsonObject(requestModel.getJsonConfig());
		List<String> products = requestModel.getProducts();
		Map<String, String> loginCredential = requestModel.getCredentials();
		UUID parentId = requestModel.getAlertId();

		WebScrapingResponse resp = new WebScrapingResponse();
		resp.setWsAuthKey(requestModel.getWsAuthKey());

		JSONObject scrapingConfig = (JSONObject) journalConfig.get(SCRAPING_CONFIF);
		JSONObject filterConfig = (JSONObject) journalConfig.get(FILTER_CONFIG);

		boolean login = (boolean) journalConfig.get(LOGIN);
		boolean applyFiltes = (boolean) journalConfig.get(APPLY_FILTER);

		try {
			deley = ((Long) scrapingConfig.get(DELEY)).intValue();
		} catch (Exception e) {
			logger.warn("Value for deley is not specified in config usin default value {}", e.getMessage());
		}
		String url = requestModel.getSource();
		String name = (String) journalConfig.get(JOURNAL_NAME);
		logger.info("Starting for {}", url);
		try {
			Thread.sleep(deley);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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
				List<String> results = extractSearchResults(scrapingConfig, 100);
				logger.info(results);
				logger.info("Got {} results ", results.size());
				List<LocalLitAlertItemModel> litAlertModelList = extractLitAlertFromResults(results, scrapingConfig,
						parentId, product);

				resp.setAlertId(requestModel.getAlertId());
				resp.setListArticles(litAlertModelList);
				JournalApiService rest = new JournalApiService();
				rest.sentResponse(resp);
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
