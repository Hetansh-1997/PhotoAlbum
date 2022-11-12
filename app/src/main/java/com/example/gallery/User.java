package com.example.gallery;

public class User {
    public String name, email;
    public String profileImage;


    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    public User(String name, String email, String profileImage) {
        this.name = name;
        this.email = email;
        this.profileImage=profileImage;
    }
}
