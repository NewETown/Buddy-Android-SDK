package com.buddy.sdk.models;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by nick on 8/25/14.
 */
public class MetadataItem {

    public String key;
    public Object value;
    public Location location;
    public Date created;
    public Date lastModified;

}
