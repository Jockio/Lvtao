package com.android.lvtao.model;

/**
 * Created by Jockio on 2015/10/11 0011.
 */
public class Ad {
    String imageUrl;
    String targetUrl;
    String title;

    public Ad(){}

    public Ad(String imageUrl, String targetUrl, String title) {
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
