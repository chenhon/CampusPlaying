package com.android.model;

/**
 * Created by Administrator on 2017/2/21 0021.
 */

public class Activity {
    /**
     * id : 00010
     * title : 活动标题
     * image : 活动头像
     * state : 0
     * wisher_count : 10
     * wisher_total : 20
     * participant_count : 7
     * creator : User
     * time : 10121212121
     * address : 字符串形式的地址
     * location : {"latitude":0.001,"longitude":0.001}
     * fee : 12.5
     * category_id : 0001
     * category_name : 分类名称
     * tags : 活动标签
     * content : 活动详情
     * notification_count : 10
     * photo_count : 5
     * created_at : 2121211212
     */

    private String id;
    private String title;
    private int image;
    private int state;
    private int wisher_count;
    private int wisher_total;
    private int participant_count;
    private User creator;
    private long time;
    private String address;
    private LocationBean location;
    private double fee;
    private String category_id;
    private String category_name;
    private String tags;
    private String content;
    private int notification_count;
    private int photo_count;
    private int created_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getWisher_count() {
        return wisher_count;
    }

    public void setWisher_count(int wisher_count) {
        this.wisher_count = wisher_count;
    }

    public int getWisher_total() {
        return wisher_total;
    }

    public void setWisher_total(int wisher_total) {
        this.wisher_total = wisher_total;
    }

    public int getParticipant_count() {
        return participant_count;
    }

    public void setParticipant_count(int participant_count) {
        this.participant_count = participant_count;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocationBean getLocation() {
        return location;
    }

    public void setLocation(LocationBean location) {
        this.location = location;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNotification_count() {
        return notification_count;
    }

    public void setNotification_count(int notification_count) {
        this.notification_count = notification_count;
    }

    public int getPhoto_count() {
        return photo_count;
    }

    public void setPhoto_count(int photo_count) {
        this.photo_count = photo_count;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public static class LocationBean {
        /**
         * latitude : 0.001
         * longitude : 0.001
         */

        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
