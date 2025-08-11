package com.journal.scrap.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalLitAlertItemModel {

	private static final long serialVersionUID = 1L;
	
	private UUID id;
	
	private UUID parentId;
	
	private LocalLitAlertModel parentModel = null ; 
	
	private String pui;
	
	private String doi;
	
	private String link;
	
	private String title;
	
	private String absCitation;
	private int absCitationCount;
	
	private String editAbsCitation;
	
	private String author;
	
	private String source;
	
	private String systemSummary;
	
	//evalsummary
	private String evaluation;
	
	private Long classification;
	
	private String classificationDecode;
	
	private Date createdDate;
	
	private Date lastUpdatedDate;
	
	private String userCreated;
	
	//localAlertTerms
//	private List<LocalLitAlertItemTermModel> itemTermModel = null ;
	
	private List<String> subClassification = null;
	
	private List<String> safetyAssessmentTypes = null;
	
	private List<String> products;
	
	private String itemSeqNo;
	
	private String asseRemarks;
	
	private String translatedStatus;
	
	private String translatedText;
	
	private boolean allowTranslationRequest;
	
	private boolean reviewed = false;
	
	public LocalLitAlertItemModel(){
		products = new ArrayList<String>();
	}
	
}
