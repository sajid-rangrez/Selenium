package com.locallit.scomed.models.twitter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Includes {
    private List<User> users;

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
}
