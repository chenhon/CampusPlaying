package com.android.model;

/**
 * Created by Administrator on 2017/2/21 0021.
 */

public class Notification {

    private int id;        //通知id
    private int creatorId; //发布者id
    private String name;   //发布者名称
    private int activityId;//依附的活动id
    private String title;  //通知标题
    private int avatarId;  //发布者头像id
    private long createdTime;//发布时间
    private String content;  //活动内容

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
