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

    private int id;
    private String user;
    private String password;
    private String name;
    private int avatar;
    private String description;
    private int followersCount;
    private int fansCount;
    private int activitysCount;
    private String relation;
    private int gender; //男0    女1

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
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


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }


    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFansCount() {
        return fansCount;
    }

    public void setFansCount(int fansCount) {
        this.fansCount = fansCount;
    }

    public int getActivitysCount() {
        return activitysCount;
    }

    public void setActivitysCount(int activitys_count) {
        this.activitysCount = activitys_count;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
