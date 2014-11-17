package com.buddy.sdk.models;

import android.location.Location;

/**
 * Created by nick on 8/26/14.
 */
public class LocationRange extends Location{
    LocationRange() {
        super("Buddy");
    }

    public double distance;
}
