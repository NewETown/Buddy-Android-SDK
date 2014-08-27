package com.buddy.sdk;

import android.location.Location;

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
public class BuddyLocationSerializer implements JsonSerializer<Location>{
    @Override public JsonElement serialize(final Location location, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        String locString = String.format(Locale.US,"%s,%s",location.getLatitude(),location.getLongitude());

        JsonElement result = new JsonPrimitive(locString);
        return result;
    }
}
