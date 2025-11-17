package com.locallit.scomed.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.journal.scrap.dao.JournalApiService;
import com.journal.scrap.model.LocalLitAlertItemModel;
import com.journal.scrap.model.LocalLitMsRequest;
import com.journal.scrap.model.LocalLitMsResponse;
import com.locallit.scomed.models.twitter.Includes;
import com.locallit.scomed.models.twitter.Tweet;
import com.locallit.scomed.models.twitter.TweetsResponse;
import com.locallit.scomed.models.twitter.User;
import com.locallit.scomed.models.youtube.CommentSnippet;
import com.locallit.scomed.models.youtube.CommentThread;
import com.locallit.scomed.models.youtube.CommentThreadListResponse;
import com.locallit.scomed.service.apiconfig.TwitterApiConfig;
import com.locallit.scomed.service.apiconfig.YouTubeApi;

@Service
public class ScoMedService {
	public static final Logger logger = LogManager.getLogger(ScoMedService.class);
	
	@Autowired
	private JournalApiService rest;
	
	@Autowired
	private YouTubeApi youTubeApi;
	
	@Autowired
	private TwitterApiConfig twitterApiConfig;
	
	public LocalLitMsResponse getComments(LocalLitMsRequest requestModel) throws JsonMappingException, JsonProcessingException {
		if(requestModel.getSource().contains("x.com")) {
			return getXComments(requestModel);
		} else if(requestModel.getSource().contains("YouTube") ||requestModel.getSource().contains("youtube.com")) {
			return getYTComments(requestModel);
		}
		return null;
	}

	public LocalLitMsResponse getXComments(LocalLitMsRequest requestModel)
			throws JsonMappingException, JsonProcessingException {
		String authkey = requestModel.getWsAuthKey();
		UUID parentId = requestModel.getAlertId();
		Map<String, String> config = getJsonObject(requestModel.getJsonConfig());
		String apiKey = requestModel.getCredentials().get("authToken");
		String postTitle = config.get("postTitle");

		logger.info(parentId);
		logger.info(authkey);

		List<LocalLitAlertItemModel> listArticles = new ArrayList<>();
		LocalLitMsResponse response = new LocalLitMsResponse().builder()
//				.retransSecurityKey(authkey)
				.alertId(parentId)
				.listArticles(listArticles)
				.wsAuthKey(authkey)
				.build();

		TweetsResponse resp = twitterApiConfig.fetchMockCommentData(config,apiKey);

		// Build user lookup from includes.users
		Map<String, User> userIndex = Optional.ofNullable(resp.getIncludes()).map(Includes::getUsers)
				.orElseGet(Collections::emptyList).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

//		response.setTotalSearchResult(resp.getData() != null ? resp.getData().size() : 0);
		System.out.println("Count: " + (resp.getMeta() != null ? resp.getMeta().getResultCount() : 0));

		for (Tweet t : Optional.ofNullable(resp.getData()).orElseGet(Collections::emptyList)) {
			LocalLitAlertItemModel model = new LocalLitAlertItemModel();
			model.setParentId(parentId);
			model.setTitle(postTitle);
			model.setAbsCitation(t.getText());

			// Resolve username via includes
			String username = Optional.ofNullable(userIndex.get(t.getAuthorId())).map(User::getUsername)
					.orElse(t.getAuthorId()); // fallback
			model.setAuthor(username);

			listArticles.add(model);
		}

		rest.sentResponse(response);
		return response;
	}

	public LocalLitMsResponse getYTComments(LocalLitMsRequest requestModel)
			throws JsonMappingException, JsonProcessingException {
		Map<String, String> config = getJsonObject(requestModel.getJsonConfig());
		String postTitle = config.get("postTitle");
		List<LocalLitAlertItemModel> listArticles = new ArrayList<>();
		String apiKey = requestModel.getCredentials().get("authToken");
		
		
		LocalLitMsResponse response = new LocalLitMsResponse().builder()
				.listArticles(listArticles)
				.alertId(requestModel.getAlertId())
				.wsAuthKey(requestModel.getWsAuthKey())
//				.retransSecurityKey(requestModel.getWsAuthKey())
				.build();

		CommentThreadListResponse apiResponse = youTubeApi.fetchCommentData(config, apiKey);

		List<CommentThread> comments = apiResponse.getItems();

		for (CommentThread comment : comments) {
			CommentSnippet commentData = comment.getCommentData();
			LocalLitAlertItemModel model = new LocalLitAlertItemModel().builder()
					.pui(comment.getId())
					.parentId(requestModel.getAlertId())
					.absCitation(commentData.getTextDisplay())
					.author(commentData.getAuthorDisplayName())
					.title(postTitle)
					.source(requestModel.getSource())
					.build();
			
			listArticles.add(model);
		}

		response.setListArticles(listArticles);

		rest.sentResponse(response);
		return response;
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
