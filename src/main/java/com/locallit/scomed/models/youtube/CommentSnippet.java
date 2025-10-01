package com.locallit.scomed.models.youtube;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentSnippet {
	private String channelId;
	private String videoId;
	private String textDisplay;
	private String textOriginal;
	private String authorDisplayName;
	private String authorProfileImageUrl;
	private String authorChannelUrl;
	private AuthorChannelId authorChannelId;
	private boolean canRate;
	private String viewerRating;
	private int likeCount;
	private String publishedAt; // or OffsetDateTime with a custom deserializer
	private String updatedAt; // same note as above
}