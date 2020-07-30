package com.amptelecom.android.app.network.model;

public class ServerResponse<T> {

    public int statusCode;
    public String statusMessage;
    public T data;
}
