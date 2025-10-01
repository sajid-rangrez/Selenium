package com.locallit.scomed.models.youtube;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
	private String kind;
	private String etag;
	private String id;
	private CommentSnippet snippet;
}
