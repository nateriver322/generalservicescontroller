package com.generalservicesportal.joborder.model;

public class MicrosoftLoginRequest {

    private String token;

    public MicrosoftLoginRequest() {
    }

    public MicrosoftLoginRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
