package com.locallit.scomed.models.twitter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//Tweet item
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {
 private String text;

 @JsonProperty("edit_history_tweet_ids")
 private List<String> editHistoryTweetIds;

 // Use String for created_at, or OffsetDateTime with a custom module
 @JsonProperty("created_at")
 private String createdAt;

 @JsonProperty("public_metrics")
 private PublicMetrics publicMetrics;

 @JsonProperty("author_id")
 private String authorId;

 private String id;

 public String getText() { return text; }
 public void setText(String text) { this.text = text; }
 public List<String> getEditHistoryTweetIds() { return editHistoryTweetIds; }
 public void setEditHistoryTweetIds(List<String> editHistoryTweetIds) { this.editHistoryTweetIds = editHistoryTweetIds; }
 public String getCreatedAt() { return createdAt; }
 public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
 public PublicMetrics getPublicMetrics() { return publicMetrics; }
 public void setPublicMetrics(PublicMetrics publicMetrics) { this.publicMetrics = publicMetrics; }
 public String getAuthorId() { return authorId; }
 public void setAuthorId(String authorId) { this.authorId = authorId; }
 public String getId() { return id; }
 public void setId(String id) { this.id = id; }
}
