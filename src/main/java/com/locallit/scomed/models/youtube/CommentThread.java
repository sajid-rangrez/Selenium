package com.locallit.scomed.models.youtube;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentThread {
	private String kind;
	private String etag;
	private String id;
	private CommentThreadSnippet snippet;
	private CommentThreadReplies replies; // may be null
	
	public CommentSnippet getCommentData(){
		return this.snippet.getTopLevelComment().getSnippet();
	}
}