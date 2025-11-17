package com.locallit.scomed.service.apiconfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.locallit.scomed.models.twitter.TweetsResponse;
import com.locallit.scomed.service.ScoMedService;

@Component
public class TwitterApiConfig {
	
	public static final Logger logger = LogManager.getLogger(TwitterApiConfig.class);

	private static final String MAX_RESULTS = "maxResults";
	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String POST_ID = "postId";
	private static final String BASE_URL = "https://api.twitter.com/2/tweets/search/recent?tweet.fields=author_id,created_at,text&query=conversation_id:";
	private static final String URL_MAX_RESULTS_PARAM = "&max_results=";
	private static final String URL_START_TIME_PARAM = "&start_time=";
	private static final String URL_END_TIME_PARAM = "&end_time=";

	public static void fetchPageInfo(String pageName, String fields, String authToken) {
		String apiUrl = "https://api.twitter.com/2/users/by/username/"+pageName+"?user.fields="+fields;
//		String apiUrl = "http://localhost:9090/mock/feed";

		try {
			HttpClient client = HttpClient.newHttpClient();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
					.header("Authorization", "Bearer " + authToken).header("Accept", "application/json").GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			System.out.println("Status Code: " + response.statusCode());
			System.out.println("Response: " + response.body());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String fetchFeedData(String searchQuery, int limit, String authToken) {
//		String apiUrl = "https://api.twitter.com/2/users/"+pageId+"/tweets?max_results="+limit+"&tweet.fields=created_at,public_metrics&start_time=2020-12-30T00:00:00.00Z&end_time=2021-01-01T00:00:00.00Z";
		String apiUrl = "https://api.twitter.com/2/tweets/search/recent?query=" + searchQuery
				+ "&tweet.fields=author_id,created_at,text,public_metrics&expansions=author_id&user.fields=username&max_results=10&media.fields=url";

		try {
			HttpClient client = HttpClient.newHttpClient();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
					.header("Authorization", "Bearer " + authToken).header("Accept", "application/json").GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			System.out.println("Status Code: " + response.statusCode());
//			System.out.println("Response: " + response.body());
			return response.body();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String fetchMockFeedData(String pageId, int limit, String authToken) {
//		String apiUrl = "https://api.twitter.com/2/users/"+pageId+"/tweets?max_results="+limit+"&tweet.fields=created_at,public_metrics&start_time=2020-12-30T00:00:00.00Z&end_time=2021-01-01T00:00:00.00Z";
		String apiUrl = "http://localhost:9090/mock/search";

		try {
			HttpClient client = HttpClient.newHttpClient();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
					.header("Authorization", "Bearer " + authToken).header("Accept", "application/json").GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			System.out.println("Status Code: " + response.statusCode());
//			System.out.println("Response: " + response.body());
			return response.body();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public TweetsResponse fetchMockCommentData(Map<String, String> config, String authToken) {

		String apiUrl = buildTwitterUrl(config);
//		String apiUrl = "https://api.twitter.com/2/tweets/search/recent?query=conversation_id:"+postId+"&tweet.fields=author_id,created_at,text&start_time=2020-12-30T00:00:00.00Z&end_time=2021-01-01T00:00:00.00Z&max_results="+limit;
//		String apiUrl = "http://localhost:9090/mock/comment";

		try {
			HttpClient client = HttpClient.newHttpClient();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
					.header("Authorization", "Bearer " + authToken).header("Accept", "application/json").GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			System.out.println("Status Code: " + response.statusCode());
//			System.out.println("Response: " + response.body());

			String data = response.body();
			logger.info(data);
			ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			TweetsResponse resp = mapper.readValue(data, TweetsResponse.class);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String buildTwitterUrl(Map<String, String> config) {
		String baseUrl = BASE_URL;
		StringBuilder url = new StringBuilder(baseUrl);

		if (config.containsKey(POST_ID)) {
			url.append(config.get(POST_ID));
		} else {
			throw new IllegalArgumentException("postId is required");
		}
		if (config.containsKey(MAX_RESULTS) && !config.get(MAX_RESULTS).isEmpty()) {
			url.append(URL_MAX_RESULTS_PARAM)
			.append(config.get(MAX_RESULTS));
		}
		if (config.containsKey(FROM) && !config.get(FROM).isEmpty()) {
			url.append(URL_START_TIME_PARAM)
			.append(config.get(FROM));
		}
		if (config.containsKey(TO) && !config.get(TO).isEmpty()) {
			url.append(URL_END_TIME_PARAM)
			.append(config.get(TO));
		}
		return url.toString();
	}

}
