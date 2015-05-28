package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Select;

import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
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

    public static List<Player> listPlayersAZ(){
        Select alphaSort_AZ = Select.from(Player.class);
        alphaSort_AZ.orderBy(StringUtil.toSQLName("playerName") + " ASC");

        return alphaSort_AZ.list();
    }

    public static boolean playerExists(String playerName){
        if (Player.find(Player.class, StringUtil.toSQLName("playerName") + " = ?", playerName).size() > 0){
            return true;
        }else{
            return false;
        }
    }

    public static long playerExists_ID(String playerName){
        List<Player> thePlayer = Player.find(Player.class, StringUtil.toSQLName("playerName") + " = ?", playerName);
        if (thePlayer.size() == 0){
            return -1;
        }else{
            return thePlayer.get(0).getId();
        }
    }
}
