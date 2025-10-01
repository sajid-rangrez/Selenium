package com.locallit.scomed.models.youtube;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentThreadListResponse {
	private String kind;
	private String etag;
	private PageInfo pageInfo;
	private List<CommentThread> items;

	public static class PageInfo {
		public int totalResults;
		public int resultsPerPage;
	}
}
