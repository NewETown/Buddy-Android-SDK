package com.buddy.sdk.models;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nick on 8/25/14.
 */
public class Checkin extends ModelBase {

    public class CheckinLocation extends Location {

        public CheckinLocation() {
            super("Buddy");
        }
        public String name;
        public String id;
    }

    public String comment;
    public String description;
}
