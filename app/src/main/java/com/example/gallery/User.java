package com.example.gallery;

public class User {
    public String name, email, hidePass;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, String hidePass) {
        this.name = name;
        this.email = email;
        this.hidePass=hidePass;
    }
}
