package com.amptelecom.android.app.chatnew.model;

public class ChatMessage {

    private String conversationid,
            sms_id,
            msgcreated,
            direction,
            inbound,
            msg_from,
            msg_to,
            message,
            media_url,
            contenttype;

    public String getConversationid() {
        return conversationid;
    }

    public void setConversationid(String conversationid) {
        this.conversationid = conversationid;
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getInbound() {
        return inbound;
    }

    public void setInbound(String inbound) {
        this.inbound = inbound;
    }

    public String getMsg_from() {
        return msg_from;
    }

    public void setMsg_from(String msg_from) {
        this.msg_from = msg_from;
    }

    public String getMsg_to() {
        return msg_to;
    }

    public void setMsg_to(String msg_to) {
        this.msg_to = msg_to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public String getContenttype() {
        return contenttype;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }
}
