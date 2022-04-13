package edu.upenn.cis.cis455.storage;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class User implements Serializable {
    public enum Type {Admin, Client};

    private final String username;
    private byte[] pwdHash;
    private final ArrayList<String> subscriptions = new ArrayList<>();
    private Type userType = Type.Client;

    public User(String username, String password){
        this.username = username;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            this.pwdHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    public static User createAdmin(){
        User user = new User("admin", "123");
        user.setUserType(Type.Admin);
        return user;
    }

    public void setUserType(Type userType) {
        this.userType = userType;
    }

    public byte[] getPwdHash() {
        return pwdHash;
    }

    public String getUsername() {
        return username;
    }

    public Type getUserType() {
        return userType;
    }

     public void subscribe(String channelName){
        this.subscriptions.add(channelName);
     }

    public ArrayList<String> getSubscriptions() {
        return subscriptions;
    }
}
