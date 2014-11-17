package com.buddy.sdk.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by nick on 8/25/14.
 */
public class Message extends ModelBase {
    public enum MessageType {
        Sent,
        Received
    }

    public String subject;
    public String body;
    public String thread;
    public String fromUserId;
    public String fromUserName;
    public String toUserId;
    public String toUserName;
    public Date sent;
    public List<String> recipients;
    public MessageType type;
    public boolean isNew;
    public Map<String,Object> warnings;
}
