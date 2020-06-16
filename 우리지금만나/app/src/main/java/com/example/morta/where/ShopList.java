package com.example.morta.where;

public class ShopList {
    private String shop_name;
    private String shop_class;
    private String address;
    private String latitude;
    private String longitude;
    private String chat_name;
    private String shop_id;

    public ShopList(String shop_name, String shop_class, String address, String latitude, String longitude, String chat_name, String shop_id)
    {
        this.shop_name = shop_name;
        this.shop_class = shop_class;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chat_name = chat_name;
        this.shop_id = shop_id;
    }
    public String getShop_name()
    {
        return this.shop_name;
    }
    public String getShop_class()
    {
        return this.shop_class;
    }
    public String getAddress()
    {
        return this.address;
    }
    public String getLatitude()
    {
        return this.latitude;
    }
    public String getLongitude()
    {
        return this.longitude;
    }
    public String getChat_name() {
        return chat_name;
    }
    public String getShop_id() {
        return shop_id;
    }
}
