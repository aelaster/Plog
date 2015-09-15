package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;

import java.util.List;

public class Player extends SugarRecord<Player> {

    public String playerName;
    public String bggUsername;
    public String playerPhoto;
    public String defaultColor;
    public String bggPassword;
    public int totalWins;
    public int totalPlays;

    public Player() {
    }

    public Player(String playerName, String bggUsername, String playerPhoto, String defaultColor, String bggPassword) {
        this.playerName = playerName;
        this.bggUsername = bggUsername;
        this.playerPhoto = playerPhoto;
        this.defaultColor = defaultColor;
        this.defaultColor = defaultColor;
    }

    public Player(String playerName, String bggUsername, String bggPassword, String defaultColor) {
        this.playerName = playerName;
        this.bggUsername = bggUsername;
        this.bggPassword = bggPassword;
        this.defaultColor = defaultColor;
    }

    public Player(String playerName, String bggUsername, String bggPassword) {
        this.playerName = playerName;
        this.bggUsername = bggUsername;
        this.bggPassword = bggPassword;
    }

    public Player(String playerName, String bggUsername) {
        this.playerName = playerName;
        this.bggUsername = bggUsername;
    }

    public Player(String playerName) {
        this.playerName = playerName;
    }

    public static List listPlayersAZ(){
        String query;
        query = " SELECT " + StringUtil.toSQLName("Player") + ".*, " +
                "("+
                " Select COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") " +
                " from " + StringUtil.toSQLName("Play") +
                " INNER JOIN " + StringUtil.toSQLName("PlayersPerPlay") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Play") + " = "+ StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Player") + " P " +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = P." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("id") +
                ") as " + StringUtil.toSQLName("totalPlays") +
                ", COUNT("+ StringUtil.toSQLName("Play") +"." + StringUtil.toSQLName("id") + ") as " + StringUtil.toSQLName("totalWins") +
                " FROM " + StringUtil.toSQLName("Player") +
                " LEFT JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                " ON " + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") +
                " LEFT JOIN "+ StringUtil.toSQLName("Play") +
                " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = " + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("score") + " >= " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("playHighScore") +
                " GROUP BY " + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " ORDER BY " + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + " ASC";
        return Player.findWithQuery(Player.class, query);
    }

    public static boolean playerExists(String playerName){
        return (Player.find(Player.class, StringUtil.toSQLName("playerName") + " = ?", playerName).size() > 0);
    }

    public static Player findPlayerByName(String playerName){
        List<Player> returnMe = Player.find(Player.class, StringUtil.toSQLName("playerName") + " = ?", playerName);
        if (returnMe.isEmpty()){
            return null;
        }else{
            return returnMe.get(0);
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
