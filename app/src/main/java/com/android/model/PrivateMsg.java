package com.android.model;

/**
 * Created by Administrator on 2017/3/18 0018.
 */

public class PrivateMsg {

    private int targetId;//私信对象id
    private int avatarId;//私信对象头像id
    private String targetName;//私信对象的昵称
    private String recentContent;//最近的一条内容
    private long recentTime;//最近一条私信发送的时间
    private int noReadCount;//未读私信数
    private int totalCount; //全部私信数

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setNoReadCount(int noReadCount) {
        this.noReadCount = noReadCount;
    }

    public int getNoReadCount() {
        return noReadCount;
    }

    public void setRecentContent(String recentContent) {
        this.recentContent = recentContent;
    }

    public String getRecentContent() {
        return recentContent;
    }

    public void setRecentTime(long recentTime) {
        this.recentTime = recentTime;
    }

    public long getRecentTime() {
        return recentTime;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
