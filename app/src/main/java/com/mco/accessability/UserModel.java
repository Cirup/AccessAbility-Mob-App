package com.mco.accessability;

public class UserModel {
    private String username;
    private String email;
    private String password;
    private int profileImg;

    public UserModel() {

    }

    public UserModel(String username, String email, String password, int profileImg){
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileImg = profileImg;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public int getProfileImg(){
        return profileImg;
    }

    public void setProfileImg(int profileImg){
        this.profileImg = profileImg;
    }
}
