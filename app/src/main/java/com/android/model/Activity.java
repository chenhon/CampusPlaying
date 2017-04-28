package com.android.model;

/**
 * Created by Administrator on 2017/2/21 0021.
 */

public class Activity {
    private int id;    //活动id
    private int creatorId;//发布者id
    private String creatorName;//发布者姓名
    private int avatarId;//发布者头像id
    private String title;  //活动标题
    private String content; //活动内容
    private int imageId;   //活动照片id
    private long time;     //活动发布时间
    private int wisherCount; //点赞数
    private int participantCount;//已参与的人数
    private int verifyStatus;//审核状态  [待审核(0)|已审核(1)|被拒绝(2)]
    private int state;//活动状态 [发起中(0)|进行中(1)|已结束(2)]

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setWisherCount(int wisherCount) {
        this.wisherCount = wisherCount;
    }

    public int getWisherCount() {
        return wisherCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setVerifyStatus(int verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public int getVerifyStatus() {
        return verifyStatus;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
