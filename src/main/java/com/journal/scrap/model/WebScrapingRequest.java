package com.journal.scrap.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebScrapingRequest {
	
//	private LocalLitAlertModel alertModel;
	String source;
	UUID alertId;
	
	private String jsonConfig;
	
	private Map<String, String> credentials;
	
	private List<String> products;
	
	private String wsAuthKey;
	
}
