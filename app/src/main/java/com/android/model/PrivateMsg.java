package com.android.model;

/**
 * Created by Administrator on 2017/3/18 0018.
 */

public class PrivateMsg {
    /**
     * target_id : 私信对象id
     * avatar : 私信对象图片id
     * recentContent : 最近的一条记录
     * noReadCount : 10
     * totalCount : 100
     */

    private String target_id;
    private String avatar;
    private String name;
    private String recentContent;
    private long recentTime;
    private int noReadCount;
    private int totalCount;

    public void setTarget_id(String target_id) {
        this.target_id = target_id;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRecentContent(String recentContent) {
        this.recentContent = recentContent;
    }

    public void setRecentTime(long recentTime) {
        this.recentTime = recentTime;
    }

    public void setNoReadCount(int noReadCount) {
        this.noReadCount = noReadCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getTarget_id() {
        return target_id;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }
    public String getRecentContent() {
        return recentContent;
    }

    public long getRecentTime() {
        return recentTime;
    }

    public int getNoReadCount() {
        return noReadCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

}
