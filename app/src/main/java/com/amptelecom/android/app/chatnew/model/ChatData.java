package com.amptelecom.android.app.chatnew.model;

import java.util.ArrayList;

public class ChatData {

    private String converstionid, message, direction, sms_id, msgcreated, from, to;

    private ArrayList<String> ccrecipients;

    public String getConverstionid() {
        return converstionid;
    }

    public void setConverstionid(String converstionid) {
        this.converstionid = converstionid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSms_id() {
        return sms_id;
    }

    public void setSms_id(String sms_id) {
        this.sms_id = sms_id;
    }

    public String getMsgcreated() {
        return msgcreated;
    }

    public void setMsgcreated(String msgcreated) {
        this.msgcreated = msgcreated;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public ArrayList<String> getCcrecipients() {
        return ccrecipients;
    }

    public void setCcrecipients(ArrayList<String> ccrecipients) {
        this.ccrecipients = ccrecipients;
    }
}
