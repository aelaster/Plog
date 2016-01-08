package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;

import java.util.List;

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

    public static Location findLocationByPlaceID(String id){
        List<Location> queery = Location.find(Location.class, StringUtil.toSQLName("locationPlaceID") + " = ?", id);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static Location findLocationByAddress(String address){
        List<Location> queery = Location.find(Location.class, StringUtil.toSQLName("locationAddress") + " = ?", address);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static Location findLocationByName(String name){
        List<Location> queery = Location.find(Location.class, StringUtil.toSQLName("locationName") + " = ?", name);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static List getAllLocationNames(){
        String query;
        query = " SELECT " + StringUtil.toSQLName("Location") + "." + StringUtil.toSQLName("locationName") +
                " FROM " + StringUtil.toSQLName("Location");
        return Location.findWithQuery(Location.class, query);
    }
}
