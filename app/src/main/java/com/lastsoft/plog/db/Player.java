package com.lastsoft.plog.db;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Select;

import java.util.List;

public class Player extends SugarRecord<Player> {

    public String playerName;
    public String bggUsername;
    public String playerPhoto;
    public String defaultColor;

    public Player() {
    }


    public Player(String playerName, String bggUsername, String playerPhoto, String defaultColor) {
        this.playerName = playerName;
        this.bggUsername = bggUsername;
        this.playerPhoto = playerPhoto;
        this.defaultColor = defaultColor;
    }

    public Player(String playerName, String bggUsername, String defaultColor) {
        this.playerName = playerName;
        this.bggUsername = bggUsername;
        this.defaultColor = defaultColor;
    }

    public Player(String playerName, String bggUsername) {
        this.playerName = playerName;
        this.bggUsername = bggUsername;
    }

    public Player(String playerName) {
        this.playerName = playerName;
    }

    public static List listPlayersAZ(){
        Select alphaSort_AZ = Select.from(Player.class);
        alphaSort_AZ.orderBy(StringUtil.toSQLName("playerName") + " ASC");

        return alphaSort_AZ.list();
    }

    public static boolean playerExists(String playerName){
        return (Player.find(Player.class, StringUtil.toSQLName("playerName") + " = ?", playerName).size() > 0);
    }

    public static long playerExists_ID(String playerName){
        List<Player> thePlayer = Player.find(Player.class, StringUtil.toSQLName("playerName") + " = ?", playerName);
        if (thePlayer.size() == 0){
            return -1;
        }else{
            return thePlayer.get(0).getId();
        }
    }

    public static List<Player> getPlayersIDs(Play play){
        return Player.findWithQuery(Player.class,
                " SELECT " + StringUtil.toSQLName("Player") + ".* " +
                        " FROM " + StringUtil.toSQLName("Player") +
                        " INNER JOIN " + StringUtil.toSQLName("PlayersPerPlay") +
                        " ON " + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") +
                        " and " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = ? ", play.getId().toString());
    }


    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Player)) {
            return false;
        }

        Player other = (Player) obj;
        return getId() == other.getId();
    }

}
