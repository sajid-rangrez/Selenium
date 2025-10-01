package com.locallit.scomed.models.twitter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Top-level response
@JsonIgnoreProperties(ignoreUnknown = true)
public class TweetsResponse {
    private List<Tweet> data;
    private Meta meta;
    private Includes includes;

    public List<Tweet> getData() { return data; }
    public void setData(List<Tweet> data) { this.data = data; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
    public Includes getIncludes() { return includes; }
    public void setIncludes(Includes includes) { this.includes = includes; }
}



// Metrics nested object
@JsonIgnoreProperties(ignoreUnknown = true)
class PublicMetrics {
    @JsonProperty("retweet_count")
    private int retweetCount;

    @JsonProperty("reply_count")
    private int replyCount;

    @JsonProperty("like_count")
    private int likeCount;

    @JsonProperty("quote_count")
    private int quoteCount;

    @JsonProperty("bookmark_count")
    private int bookmarkCount;

    @JsonProperty("impression_count")
    private long impressionCount;

    public int getRetweetCount() { return retweetCount; }
    public void setRetweetCount(int retweetCount) { this.retweetCount = retweetCount; }
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getQuoteCount() { return quoteCount; }
    public void setQuoteCount(int quoteCount) { this.quoteCount = quoteCount; }
    public int getBookmarkCount() { return bookmarkCount; }
    public void setBookmarkCount(int bookmarkCount) { this.bookmarkCount = bookmarkCount; }
    public long getImpressionCount() { return impressionCount; }
    public void setImpressionCount(long impressionCount) { this.impressionCount = impressionCount; }
}


