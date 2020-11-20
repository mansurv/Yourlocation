package com.netmontools.lookatnet.ui.point.model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"name", "bssid"}, unique = true)})
public class DataModel {

    public DataModel() {
    }

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String bssid;
    private String name;
    private double lon;
    private double lat;

    public long getId() { return id; }
    public void setId(long id) {this.id = id; }

    @Nullable
    public String getName() { return name; }
    public void setName(@Nullable String name){ this.name = name; }

    @Nullable
    public String getBssid() { return bssid; }
    public void setBssid(@Nullable String bssid){ this.bssid = bssid; }

    public double getLon() { return lon; }
    public void setLon(double lon) {this.lon = lon; }

    public double getLat() { return lat; }
    public void setLat(double lat) {this.lat = lat; }
}

