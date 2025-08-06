package com.journal.scrap.entities;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Product {
	
	private Long id;

	private String name;
	
	private List<Long> articles;

}
