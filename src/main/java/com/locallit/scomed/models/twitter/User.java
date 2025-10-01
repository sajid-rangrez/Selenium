package com.locallit.scomed.models.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//User object from includes.users
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
private String id;
private String name;
private String username;

public String getId() { return id; }
public void setId(String id) { this.id = id; }
public String getName() { return name; }
public void setName(String name) { this.name = name; }
public String getUsername() { return username; }
public void setUsername(String username) { this.username = username; }
}
