package com.android.model;

/**
 * Created by Administrator on 2017/3/20 0020.
 */

public class Comment {

    /**
     * 评论类型
     * 有活动评论
     * 照片评论
     * 通知评论
     */
    public static final int ACTIVITY_TYPE = 1;
    public static final int PHOTO_TYPE = 2;
    public static final int NOTIFICATION_TYPE = 3;

    /**
     * id : 评论d的id
     * creatorId 评论人id
     * name : 评论人名称
     * avatarId : 评论人头像id
     * relation : 评论人与评论对象的关系
     * createdTime : 评论创建时间
     * content : 评论内容
     * parentId : 评论对象的id
     */

    private int id;
    private int creatorId;
    private String name;
    private int avatarId;
    private String relation;
    private long createdTime;
    private String content;
    private int parentId;
    private int attachType;//评论对应的类型
    private int attachId;//评论对应的类型的对象id
    private int attachImage;//依附的照片
    private String attachContent;//依附内容（活动和照片是title， 图片是description ）
    //加入依附的活动（或照片）的发布者信息（昵称和id）

    private int attachCreatorId;
    private String attachCreatorName;

    public void setAttachContent(String attachContent) {
        this.attachContent = attachContent;
    }

    public String getAttachContent() {
        return attachContent;
    }

    public void setAttachCreatorId(int attachCreatorId) {
        this.attachCreatorId = attachCreatorId;
    }

    public int getAttachCreatorId() {
        return attachCreatorId;
    }

    public void setAttachCreatorName(String attachCreatorName) {
        this.attachCreatorName = attachCreatorName;
    }

    public String getAttachCreatorName() {
        return attachCreatorName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setAttachType(int attachType) {
        this.attachType = attachType;
    }

    public void setAttachId(int attachId) {
        this.attachId = attachId;
    }

    public void setAttachImage(int attachImage) {
        this.attachImage = attachImage;
    }

    public int getAttachImage() {
        return attachImage;
    }

    public int getId() {
        return id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public String getName() {
        return name;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public String getRelation() {
        return relation;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getContent() {
        return content;
    }

    public int getParentId() {
        return parentId;
    }

    public int getAttachType() {
        return attachType;
    }

    public int getAttachId() {
        return attachId;
    }
}
