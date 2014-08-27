package com.buddy.sdk;

import android.location.Location;

import com.buddy.sdk.models.ModelBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by shawn on 7/6/14.
 */
public class JsonEnvelopeDeserializer<T> implements JsonDeserializer<JsonEnvelope<Object>>
{
    private Class clazz;

    private Gson gson;

    public JsonEnvelopeDeserializer(Class<T> clazz) {
        this.clazz = clazz;
        gson = JsonEnvelopeDeserializer.makeGsonDeserializer();
    }

    public static Gson makeGsonDeserializer(){
        return new GsonBuilder()
                .registerTypeAdapter(Location.class, new BuddyLocationDeserializer())
                .registerTypeAdapter(Date.class, new BuddyDateDeserializer())
                .create();
    }

    @Override
    public JsonEnvelope<Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException
    {
        JsonObject jsonObj = json.getAsJsonObject();
        Object result = null;

        JsonElement element = jsonObj.get("result");

        if (element.isJsonObject()) {

            if (clazz != null && !JsonObject.class.isAssignableFrom(clazz)) {
                result = gson.fromJson(element.getAsJsonObject(), clazz);
            }
            else {
                result = element.getAsJsonObject();
            }

            if (result instanceof ModelBase) {
                ((ModelBase)result).setJsonObject(element.getAsJsonObject());
            }
        }
        else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isString()) {
                result = primitive.getAsString();
            }
            else if (primitive.isBoolean()) {
                result = primitive.getAsBoolean();
            }
            else if (primitive.isNumber()) {
                result = primitive.getAsLong();
            }
        }
        else {
            throw new JsonParseException("Can't deal with JSON: " + element.toString());
        }



        JsonEnvelope<Object> env = new JsonEnvelope<Object>(jsonObj, result);

        return env;
    }
}
