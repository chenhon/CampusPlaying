package com.android.model;

/**
 * Created by Administrator on 2017/2/21 0021.
 */

public class Photo {
    /**
     * id  : 00010
     * creator_id  : 00010
     * activity_id  : 00010
     * media_id  : 00010
     * description  : 照片说明
     * created_at  : 12121212
     * comment_count  : 15
     */

    private String id;
    private String creator_id;
    private String activity_id;
    private String media_id;
    private String description;
    private int created_at;
    private int comment_count;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(String activity_id) {
        this.activity_id = activity_id;
    }

    public String getMedia_id() {
        return media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }
}
