package com.android.model;

/**
 * Created by Administrator on 2017/2/21 0021.
 */

public class User {
    public static final int RELATION_MYSELF = 0; //待定
    public static final int RELATION_FOLLOWER = 1;
    public static final int RELATION_FAN = 2;
    public static final int RELATION_FRIEND = 3;
    /**
     * id : 00010
     * name : 用户名
     * avatar : 头像
     * description  : 签名
     * followers_count : 10
     * fans_count : 12
     * activitys_count : 20
     * relation : 0
     */

    private String id;
    private String name;
    private int avatar;
    private String description;
    private int followers_count;
    private int fans_count;
    private int activitys_count;
    private int relation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(int followers_count) {
        this.followers_count = followers_count;
    }

    public int getFans_count() {
        return fans_count;
    }

    public void setFans_count(int fans_count) {
        this.fans_count = fans_count;
    }

    public int getActivitys_count() {
        return activitys_count;
    }

    public void setActivitys_count(int activitys_count) {
        this.activitys_count = activitys_count;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }
}
