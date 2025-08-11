package com.journal.scrap.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebScrapingReqModel {
	
	private LocalLitAlertModel alertModel;
	
	private String journalConfig;
	
	private Map<String, String> credentials;
	
	private List<String> products;
	
	private String securityToken;
}
