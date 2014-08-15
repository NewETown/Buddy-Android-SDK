package com.buddy.buddysampleapp.util;

/**
 * Created by everett on 8/14/14.
 */
public class AppImage {
    private final String id;
    private final String caption;

    public AppImage(String id, String caption) {
        this.id = id;
        this.caption = caption;
    }

    public String getCaption() {
        return this.caption;
    }

    public String getId() {
        return this.id;
    }
}