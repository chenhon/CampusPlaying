package com.android.model;

/**
 * Created by Administrator on 2017/2/21 0021.
 */

public class Notification {
    /**
     * id : 00010
     * creator_id : 00010
     * activity_id  : 00010
     * title : 通知标题
     * content : 通知内容
     * comment_count : 10
     * created_at  : 1122121212
     */

    private String id;
    private String creator_id;
    private String activity_id;
    private String title;
    private String content;
    private int comment_count;
    private int created_at;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }
}
