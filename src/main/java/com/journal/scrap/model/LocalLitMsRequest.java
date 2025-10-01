package com.journal.scrap.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class LocalLitMsRequest {
	
//	private LocalLitAlertModel alertModel;
	private UUID alertId;
	
	private String source;
	
	private String wsAuthKey;
	
	private String jsonConfig;
	
	private List<String> products;
	
	private Map<String, String> credentials;
	
}
