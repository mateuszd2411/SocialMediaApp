package com.matt.socialmediaapp.notifications;

public class Token {
    /*
    An FCM Token, or much commonly know as a registrationToken.
    An ID issued by yhe GCMconnection servers to the client app that allows it to receive messages*/

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
