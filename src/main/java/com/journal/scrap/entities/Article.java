package com.journal.scrap.entities;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class Article {


	private Journal journal;
	
	private String product;
	private String title;
	private String doi;
	private String abs;
	private String link;
	private String authors;

}
