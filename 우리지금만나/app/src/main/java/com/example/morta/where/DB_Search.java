package com.example.morta.where;

public class DB_Search {
    String db_id;
    String db_name;
    String db_class;
    String db_address;
    String db_lat;
    String db_lon;
    String db_feature;

    public DB_Search(String db_id, String db_name, String db_class, String db_address, String db_lat, String db_lon, String db_feature)
    {
        this.db_id = db_id;
        this.db_name = db_name;
        this.db_class = db_class;
        this.db_address = db_address;
        this.db_lat = db_lat;
        this.db_lon = db_lon;
        this.db_feature = db_feature;
    }

    public String getDb_id() {
        return db_id;
    }

    public String getDb_name() {
        return db_name;
    }

    public String getDb_class() {
        return db_class;
    }

    public String getDb_address() {
        return db_address;
    }

    public String getDb_lat() {
        return db_lat;
    }

    public String getDb_lon() {
        return db_lon;
    }

    public String getDb_feature() {
        return db_feature;
    }
}
