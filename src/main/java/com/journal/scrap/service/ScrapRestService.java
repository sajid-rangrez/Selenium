package com.journal.scrap.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
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

		WebScrapingResponse response = new WebScrapingResponse();
		response.setWsAuthKey(requestModel.getWsAuthKey());

		JSONObject scrapingConfig = (JSONObject) journalConfig.get(SCRAPING_CONFIF);
		JSONObject filterConfig = (JSONObject) journalConfig.get(FILTER_CONFIG);
		String sourceName = (String) journalConfig.get(JOURNAL_NAME);

		boolean login = (boolean) journalConfig.get(LOGIN);
		boolean applyFiltes = (boolean) journalConfig.get(APPLY_FILTER);

		try {
			deley = ((Long) scrapingConfig.get(DELEY)).intValue();
		} catch (Exception e) {
			logger.warn("Value for deley is not specified in config usin default value {}", e.getMessage());
		}
		String url = requestModel.getSource();
		logger.info("Starting for {}", url);
		
		deley();

		// searching for each product in given journal
		for (String product : products) {
			try {
				try {
					login = openJournal(url, login, journalConfig, loginCredential);
				} catch (Exception e) {
					logger.error("Got error while login {}", e.getMessage());
				}
				String searchPath = (String) scrapingConfig.get(SEARCH_INPUT_SELECTOR);

				deley();
				searchProduct(searchPath, product);
				// Apply filters
				if (applyFiltes) {
					deley();
					applyFilters(filterConfig);
				}
				deley();
				List<String> results = extractSearchResults(scrapingConfig, 100);
				int numberOfSearchResults = results.size();
				logger.info(results);
				logger.info("Got {} results ", numberOfSearchResults);
				
				if(numberOfSearchResults == 0) {
					takeScreenShot(sourceName, product);
				}
				
				List<LocalLitAlertItemModel> litAlertModelList = extractLitAlertFromResults(results, scrapingConfig,
						parentId, product);

				response.setAlertId(requestModel.getAlertId());
				response.setListArticles(litAlertModelList);
				JournalApiService rest = new JournalApiService();
				rest.sentResponse(response);
			} catch (Exception e) {
				logger.error("Got error while scaraping product {} on {}, {}", product, url, e.getMessage());
			}
		}
		if (driver != null) {
			driver.quit();
			logger.info("driver Quit");
		}
		logger.info("Script complete!");
		return response;
	}

	private void takeScreenShot(String sourceName,String product) {
		try {
			FileUtils.copyFile(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE),
					new File(EVIDENCE_DIR +"/"+ sourceName+"-"+product
							+ new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date())						+ ".png"));
		} catch (WebDriverException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
