package com.example.android.cleancity;

public class dustbin {
    private long id;
    private long level;
    private Double longitude;
    private Double latitude;

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

    public dustbin(long id, long level, Double longitude, Double latitude) {
        this.id = id;
        this.level = level;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
