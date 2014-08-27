package com.buddy.sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nick on 8/25/14.
 */
public class BinaryModelBase extends ModelBase {
    public String contentType;
    public int contentLength;
    public String signedUrl;
}
