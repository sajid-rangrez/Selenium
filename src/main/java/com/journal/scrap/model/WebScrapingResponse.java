package com.journal.scrap.model;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebScrapingResponse {
	
	private UUID alertId	;
	
	private List<LocalLitAlertItemModel> listArticles;
	
	private String retransSecurityKey;
	
	private String wsAuthKey;

}
