package com.locallit.scomed.models.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//Meta object
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {
 @JsonProperty("newest_id")
 private String newestId;

 @JsonProperty("oldest_id")
 private String oldestId;

 @JsonProperty("result_count")
 private int resultCount;

 @JsonProperty("next_token")
 private String nextToken;

 public String getNewestId() { return newestId; }
 public void setNewestId(String newestId) { this.newestId = newestId; }
 public String getOldestId() { return oldestId; }
 public void setOldestId(String oldestId) { this.oldestId = oldestId; }
 public int getResultCount() { return resultCount; }
 public void setResultCount(int resultCount) { this.resultCount = resultCount; }
 public String getNextToken() { return nextToken; }
 public void setNextToken(String nextToken) { this.nextToken = nextToken; }
}