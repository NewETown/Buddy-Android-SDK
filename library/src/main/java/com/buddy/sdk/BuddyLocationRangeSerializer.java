package com.buddy.sdk;

import android.location.Location;

import com.buddy.sdk.models.LocationRange;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

/**
 * Created by nick on 8/26/14.
 */
public class BuddyLocationRangeSerializer implements JsonSerializer<LocationRange>{

	public static String serializeCore(LocationRange locationRange) {
		return String.format(Locale.US,"%s,%s,%s",locationRange.getLatitude(),locationRange.getLongitude(),locationRange.distance);
	}

    @Override public JsonElement serialize(final LocationRange locationRange, final Type typeOfSrc, final JsonSerializationContext context) {
        
        String locString = serializeCore(locationRange);

        JsonElement result = new JsonPrimitive(locString);
        return result;
    }
}
