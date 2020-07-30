package com.amptelecom.android.app.chatnew.model;

import java.util.ArrayList;

public class ChatConversationsResponse {

    private String statusCode, statusMessage;

    private ArrayList<ChatData> data;

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

    public ArrayList<ChatData> getData() {
        return data;
    }

    public void setData(ArrayList<ChatData> data) {
        this.data = data;
    }
}
