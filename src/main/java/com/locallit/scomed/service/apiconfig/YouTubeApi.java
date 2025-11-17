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
import com.locallit.scomed.models.youtube.CommentThreadListResponse;


@Component
public class YouTubeApi {

	public static final Logger logger = LogManager.getLogger(YouTubeApi.class);
	
	
	private static final String MAX_RESULTS = "maxResults";
	private static final String KEY = "key";
	private static final String POST_ID = "postId";
	private static final String URL_VIDEO_ID_PARAM = "&videoId=";
	private static final String BASE_URL = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&order=time&textFormat=plainText";
	private static final String URL_MAX_RESULTS_PARAM = "&max_results=";
	private static final String URL_KEY_PARAM = "&key=";

	public CommentThreadListResponse fetchCommentData(Map<String, String> params, String apiKey) {
		String apiUrl = buildYouTubeCommentsUrl(params, apiKey);
//		String apiUrl = "http://localhost:9090/mock/yt/comments";
//		String apiUrl = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&videoId="+videoId+"&key="+token+"&maxResults="+limit;

		try {
			HttpClient client = HttpClient.newHttpClient();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).header("Accept", "application/json")
					.GET().build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			String data = response.body();
			logger.info("Got Response code {}.", response.statusCode());
			logger.info(data);

			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			CommentThreadListResponse resp = mapper.readValue(data, CommentThreadListResponse.class);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String buildYouTubeCommentsUrl(Map<String, String> config, String apiKey) {
		String baseUrl = BASE_URL;
		StringBuilder url = new StringBuilder(baseUrl);

		if (config.containsKey(POST_ID)) {
			url.append(URL_VIDEO_ID_PARAM).append(config.get(POST_ID));
		} else {
			throw new IllegalArgumentException("postId is required!");
		}
		if (config.containsKey(MAX_RESULTS) && !config.get(MAX_RESULTS).isEmpty()) {
			url.append(URL_MAX_RESULTS_PARAM).append(config.get(MAX_RESULTS));
		}
		url.append(URL_KEY_PARAM).append(apiKey);

		return url.toString();
	}
}
