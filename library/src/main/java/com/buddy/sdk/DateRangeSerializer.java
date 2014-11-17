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
public class DateRangeSerializer implements JsonSerializer<DateRange>{


    public static String serializeCore(DateRange dateRange) {
        return String.format(Locale.US,"%d-%d", dateRange.getStart().getTime(), dateRange.getEnd().getTime());
    }
    @Override public JsonElement serialize(final DateRange dateRange, final Type typeOfSrc, final JsonSerializationContext context) {
       
        String locString = serializeCore(dateRange);

        JsonElement result = new JsonPrimitive(locString);
        return result;
    }
}
