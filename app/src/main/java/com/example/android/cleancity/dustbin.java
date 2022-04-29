package com.example.android.cleancity;

public class dustbin {
    private long id;
    private long level;
    private Double longitude;
    private Double latitude;
    private long area;
    private float rate;

    public long getId() {
        return id;
    }

    public long getLevel() {
        return level;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public long getArea() {
        return area;
    }

    public float getRate() {
        return rate;
    }

    public dustbin(long id, long level, Double latitude, Double longitude, long area, float rate) {
        this.id = id;
        this.level = level;
        this.longitude = longitude;
        this.latitude = latitude;
        this.area = area;
        this.rate = rate;
    }
}
