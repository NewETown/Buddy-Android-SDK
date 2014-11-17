package com.buddy.sdk.models;

/**
 * Created by nick on 8/25/14.
 */
public class UserListItem {
    public enum UserListItemType {
        User,
        UserList

    }

    public String id;
    public UserListItemType itemType;
}
