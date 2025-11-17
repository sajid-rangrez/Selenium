package com.journal.scrap.model;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalLitMsResponse {
	
	private UUID alertId	;
	
	private List<LocalLitAlertItemModel> listArticles;
	
//	private String retransSecurityKey;
	
	private String wsAuthKey;
	
//	private int totalSearchResult;

}
