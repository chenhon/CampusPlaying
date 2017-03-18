package com.android.model;

/**
 * 私信
 */

public class Private {
    /**
     * direction : 私信发送方向
     * content : 私信内容
     * created_at : 创建时间，unix时间戳
     */
public static final int SEND_TYPE = 0; //表示自己发出的
    public static final int GET_TYPE = 1;//表示对方发出的
    private int direction;
    private String content;
    private long created_at;

    public Private() {}
    public Private(int direction, String content) {
        this.direction = direction;
        this.content = content;
    }
    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public int getDirection() {
        return direction;
    }

    public String getContent() {
        return content;
    }

    public long getCreated_at() {
        return created_at;
    }
}
