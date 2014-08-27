package com.buddy.sdk.models;

import android.location.Geocoder;
import android.location.Location;

import com.google.gson.JsonObject;

import java.util.Date;

/**
 * Created by shawn on 7/6/14.
 */
public class ModelBase {
    public String id;
    public Date created;
    public Date lastModified;
    public Location location;

    private JsonObject jsonObject;

    public void setJsonObject(JsonObject json) {
        jsonObject = json;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }


}
