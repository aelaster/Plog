package com.lastsoft.plog.db;

import com.orm.SugarRecord;

public class Location extends SugarRecord<Location> {

    public String locationName;
    public String locationAddress;
    public String locationPlaceID;
    public double locationLat;
    public double locationLong;

    public Location() {
    }

    public Location(String locationName, String locationAddress, String locationPlaceID, double locationLat, double locationLong) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationPlaceID = locationPlaceID;
        this.locationLat = locationLat;
        this.locationLong = locationLong;
    }

}
