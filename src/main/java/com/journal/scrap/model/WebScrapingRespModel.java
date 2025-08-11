package com.journal.scrap.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebScrapingRespModel {
	
	private LocalLitAlertModel alertModel;
	
	private List<LocalLitAlertItemModel> listArticles;
	
	private String retransSecurityKey;

}
