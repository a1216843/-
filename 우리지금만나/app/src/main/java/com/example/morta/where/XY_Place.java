package com.example.morta.where;

public class XY_Place {
    private String latitude;
    private String longitude;

    public XY_Place() {}
    public XY_Place(String latitude, String longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public  void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public  void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
