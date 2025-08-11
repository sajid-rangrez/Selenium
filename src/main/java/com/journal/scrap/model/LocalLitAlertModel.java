package com.journal.scrap.model;


import java.util.Date;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LocalLitAlertModel {

	private static final long serialVersionUID = 1L;
	
	private UUID id;
	
	private String compUnit;
	
	private Long country;
	
	private String countryDecode;
	
	private Long language;
	
	private String languageDecode;
	
	private String product;
	
	private Long srcType;
	
	private String srcTypeDecode;
	
	private String source;
	
	private String searchUrl;
	
	private String searchTitle;
	
	private Integer year;
	
	private Integer week;
	
	private Long alertExeStatus;
	
	private String alertExeStatusDecode;
	
	private Integer totalLitCount;
	
	private Date alertDueOn;
	
	private Date createdDate;
	
	private Date lastUpdatedDate;
	
	private String userCreated;
	
	private UUID parentId;
	
	private String remarks;
	
	private String alertBucketId;
	
	
//	public boolean isAlertOverDue() {
//	    return alertDueOn.after(new Date());
//	}

}

