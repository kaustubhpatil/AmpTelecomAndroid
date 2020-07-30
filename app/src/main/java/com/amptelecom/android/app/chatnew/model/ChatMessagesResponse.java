package com.amptelecom.android.app.chatnew.model;

import java.util.ArrayList;

public class ChatMessagesResponse {

    private String statusCode, statusMessage;

    private ArrayList<ChatMessage> data;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public ArrayList<ChatMessage> getData() {
        return data;
    }

    public void setData(ArrayList<ChatMessage> data) {
        this.data = data;
    }
}
