package com.scrap.journal.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrossrefApiService {
	 public static void main(String[] args) throws Exception {
	        String doi = "10.4103/sja.sja_654_22"; // Replace with actual DOI
	        String apiUrl = "https://api.crossref.org/works/" + doi;

	        HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(apiUrl))
	                .GET()
	                .build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

	        ObjectMapper mapper = new ObjectMapper();
	        String json = response.body();
	        System.out.println(json);
	        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	        Root root = mapper.readValue(json, Root.class);
	        SimplifiedWork work = root.getMessage();

	        // Parse top-level response
//	        System.out.println(response.body());
//	        Map<String, Object> resp = (Map<String, Object>) response;
//	        System.out.println(resp);

	        // Get the simplified message
//	        SimplifiedWork work = mapper.convertValue(root.getMessage(), SimplifiedWork.class);

	        System.out.println("Title: " + work.getTitle());
	        System.out.println("Author: " + (work.getAuthor().isEmpty() ? "N/A" : work.getAuthor().get(0)));
	        System.out.println("Abstract: " + work.getAbstract());
	        System.out.println("Published Date: " + work.getPublished().getDateParts());
	        System.out.println("ISSN :" + work.getIssn());
	        System.out.println("URL :" + work.getURL());
	        System.out.println("journal :" + work.getContainerTitle());
	    }

	    // Root class (to access "message")
	    public static class Root {
	        private SimplifiedWork message;
	        private String status;
	        @JsonProperty("message-version")
	        private String messageVersion;
	        @JsonProperty("message-type")
	        private String messageType;
	        
	        public SimplifiedWork getMessage() {
	            return message;
	        }

	        public void setMessage(SimplifiedWork message) {
	            this.message = message;
	        }

			public String getStatus() {
				return status;
			}

			public void setStatus(String status) {
				this.status = status;
			}

			public String getMessageVersion() {
				return messageVersion;
			}

			public void setMessageVersion(String messageVersion) {
				this.messageVersion = messageVersion;
			}

			public String getMessageType() {
				return messageType;
			}

			public void setMessageType(String messageType) {
				this.messageType = messageType;
			}
	        
	        
	    }

	    // SimplifiedWork class (your custom mapped data)
	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class SimplifiedWork {
	        private List<String> title;
	        @JsonProperty("author")
	        private List<Author> author;

	        @JsonProperty("abstract")
	        private String _abstract;

	        @JsonProperty("ISSN")
	        private List<String> issn;

	        @JsonProperty("URL")
	        private String URL;

	        private String issue;
	        private String publisher;

	        @JsonProperty("container-title")
	        private List<String> containerTitle;

	        @JsonProperty("published")
	        private DateParts published;

	        @JsonProperty("content-updated")
	        private DateParts contentUpdated;

	        // Getters and Setters
	        public List<String> getTitle() { return title; }
	        public List<Author> getAuthor() { return author; }
	        public String getAbstract() { return _abstract; }
	        public List<String> getIssn() { return issn; }
	        public String getURL() { return URL; }
	        public String getIssue() { return issue; }
	        public String getPublisher() { return publisher; }
	        public List<String> getContainerTitle() { return containerTitle; }
	        public DateParts getPublished() { return published; }
	        public DateParts getContentUpdated() { return contentUpdated; }
	        
	        @JsonIgnoreProperties(ignoreUnknown = true)
	        public static class Author {
	            private String given;
	            private String family;
	            private String name;

	            public String getName() { return name; }
	            public String getGiven() { return given; }
	            public String getFamily() { return family; }
	            @Override
	            public String toString() {
	            	return getGiven();
	            }
	        }

	        @JsonIgnoreProperties(ignoreUnknown = true)
	        public static class DateParts {
	            @JsonProperty("date-parts")
	            private List<List<String>> dateParts;

	            public List<List<String>> getDateParts() { return dateParts; }
	        }
	    }
}
