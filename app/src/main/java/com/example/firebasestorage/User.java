package com.example.firebasestorage;

public class User {

    String name;
    String email;
    String pass;
    String image;
    String uid;

    public User() {
    }

    public User(String name, String email, String pass, String image,String uid) {
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
