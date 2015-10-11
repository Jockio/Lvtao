package com.android.lvtao.model;

/**
 * Created by john on 2015/9/22 0022.
 */
public class Commodity {
    String name;
    String newPrice;
    String imageUrl;
    String goodsUrl;
    String oldPrice;

    public Commodity(){}

    public Commodity(String name, String newPrice, String imageUrl, String goodsUrl, String oldPrice) {
        this.name = name;
        this.newPrice = newPrice;
        this.imageUrl = imageUrl;
        this.goodsUrl = goodsUrl;
        this.oldPrice = oldPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(String newPrice) {
        this.newPrice = newPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGoodsUrl() {
        return goodsUrl;
    }

    public void setGoodsUrl(String goodsUrl) {
        this.goodsUrl = goodsUrl;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }
}
