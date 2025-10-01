package com.locallit.scomed.models.youtube;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentThreadSnippet {
	private String channelId;
	private String videoId;
	private Comment topLevelComment;
	private boolean canReply;
	private int totalReplyCount;
	private boolean isPublic;
}