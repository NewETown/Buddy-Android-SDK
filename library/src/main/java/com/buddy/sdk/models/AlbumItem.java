package com.buddy.sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nick on 8/25/14.
 */
public class AlbumItem extends ModelBase {

    public enum AlbumItemType {
        Picture,
        Video
    }

    public AlbumItemType itemType;
    public String caption;
    public String itemId;

}
