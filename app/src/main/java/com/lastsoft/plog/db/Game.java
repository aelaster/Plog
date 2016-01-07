package com.lastsoft.plog.db;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class Game extends SugarRecord<Game> {

    public String gameName;
    public String gameBGGID;
    public String gameBGGCollectionID;
    public String gameImage;
    public String gameThumb;
    public String gameBoxImage;
    public boolean expansionFlag;
    public int tbtCount;
    public int taggedToPlay;
    public int playCount;
    public long recentPlay;
    public boolean collectionFlag;

    public Game(){ }

    public Game(String gameName, String gameBGGID, String gameBGGCollectionID, String gameImage, String gameThumb, boolean expansionFlag, boolean collectionFlag) {
        this.gameName = gameName;
        this.gameBGGID = gameBGGID;
        this.gameBGGCollectionID = gameBGGCollectionID;
        this.gameImage = gameImage;
        this.gameThumb = gameThumb;
        this.expansionFlag = expansionFlag;
        this.collectionFlag = collectionFlag;
    }

    public Game(String gameName, String gameBGGID, String gameBGGCollectionID, String gameImage, String gameThumb, boolean expansionFlag) {
        this.gameName = gameName;
        this.gameBGGID = gameBGGID;
        this.gameBGGCollectionID = gameBGGCollectionID;
        this.gameImage = gameImage;
        this.gameThumb = gameThumb;
        this.expansionFlag = expansionFlag;
    }


    public Game(String gameName, String gameBGGID, String gameBGGCollectionID, String gameThumb, boolean expansionFlag) {
        this.gameName = gameName;
        this.gameBGGID = gameBGGID;
        this.gameBGGCollectionID = gameBGGCollectionID;
        this.gameImage = null;
        this.gameThumb = gameThumb;
        this.expansionFlag = expansionFlag;
    }

    public Game(String gameName, String gameBGGID, String gameBGGCollectionID, String gameThumb) {
        this.gameName = gameName;
        this.gameBGGID = gameBGGID;
        this.gameBGGCollectionID = gameBGGCollectionID;
        this.gameImage = null;
        this.gameThumb = gameThumb;
        this.expansionFlag = false;
    }

    public Game(String gameName, boolean expansionFlag) {
        this.gameName = gameName;
        this.gameBGGID = "";
        this.gameBGGCollectionID = "";
        this.gameImage = null;
        this.gameThumb = null;
        this.expansionFlag = expansionFlag;
    }

    public Game(String gameName, String gameThumb, boolean expansionFlag) {
        this.gameName = gameName;
        this.gameBGGID = "";
        this.gameBGGCollectionID = "";
        this.gameImage = null;
        this.gameThumb = gameThumb;
        this.expansionFlag = expansionFlag;
    }

    public Game(String gameName) {
        this.gameName = gameName;
        this.gameBGGID = "";
        this.gameBGGCollectionID = "";
        this.gameImage = null;
        this.gameThumb = null;
        this.expansionFlag = false;
    }

    public static Game findGameByName(String name){
        List<Game> queery = Game.find(Game.class, StringUtil.toSQLName("gameName") + " = ?", name);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static Game findGameByBGGID(String bggID){
        List<Game> queery = Game.find(Game.class, StringUtil.toSQLName("gameBGGID") + " = ?", bggID);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static Game findGameByName_NoCase(String name) {
        List<Game> queery = Game.findWithQuery(Game.class, "Select * from " + StringUtil.toSQLName("Game") + " where " + StringUtil.toSQLName("gameName") + " = ? COLLATE NOCASE", name);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static Game yearsPlays(Game game, int year){

        String query = "Select "  + StringUtil.toSQLName("Game") + ".*, COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") as " + StringUtil.toSQLName("tbtCount") +
                " from " + StringUtil.toSQLName("Play") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("Play") + " = "+ StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("GamesPerPlay") + "." +  StringUtil.toSQLName("game") + " = ? " +
                " AND STRFTIME('%Y', DATETIME(SUBSTR(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? ";

        //Log.d("V1", query);
        return Game.findWithQuery(Game.class, query, game.getId().toString(), year + "").get(0);
    }

    public static List<Game> findBaseGames(String mSearchQuery, int sortType, int year){
        String query;
        if (mSearchQuery.contains("'")) {
            mSearchQuery = mSearchQuery.replaceAll("'", "''");
        }
        query = " SELECT G.*, COUNT(P." + StringUtil.toSQLName("id") + ") AS " + StringUtil.toSQLName("playCount") + ", MAX(P." + StringUtil.toSQLName("playDate") + ") AS " + StringUtil.toSQLName("recentPlay") +
                ", ("+
                " Select COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") " +
                " from " + StringUtil.toSQLName("Play") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("Play") + " = "+ StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("GamesPerPlay") + "." +  StringUtil.toSQLName("game") + " = G." + StringUtil.toSQLName("id") +
                " AND STRFTIME('%Y', DATETIME(SUBSTR(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? " +
                " ) as " + StringUtil.toSQLName("tbtCount") +
                " FROM " + StringUtil.toSQLName("Game") + " G " +
                " LEFT JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                " ON G." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("game") +
                " LEFT JOIN " + StringUtil.toSQLName("Play") + " P " +
                " ON GPP." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id");
        if (!mSearchQuery.equals("")) {
            query = query + " WHERE G. " + StringUtil.toSQLName("expansionFlag") + " = 0" +
                   " AND G." + StringUtil.toSQLName("gameName") + " LIKE '%" + mSearchQuery + "%'";
        }else {
            query = query + " WHERE G. " + StringUtil.toSQLName("expansionFlag") + " = 0";
        }
        query = query + " GROUP BY G." + StringUtil.toSQLName("gameName");
        switch (sortType) {
            case 0:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 1:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " DESC";
                break;
            case 2:
                query = query + " ORDER BY COUNT(P." + StringUtil.toSQLName("id") + ") DESC, MAX(P." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 3:
                query = query + " ORDER BY COUNT(P." + StringUtil.toSQLName("id") + ") ASC, MAX(P." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 4:
                query = query + " ORDER BY MAX(P." + StringUtil.toSQLName("playDate") + ") DESC, G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 5:
                query = query + " ORDER BY MAX(P." + StringUtil.toSQLName("playDate") + ") ASC, G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
        }
        return Game.findWithQuery(Game.class, query, year + "");
    }

    public static List<Game> findAllGames_GameGroup(GameGroup group, int sortType){
        String query;
        query = " SELECT G.*, COUNT(P." + StringUtil.toSQLName("id") + ") AS " + StringUtil.toSQLName("playCount") + ", MAX(P." + StringUtil.toSQLName("playDate") + ") AS " + StringUtil.toSQLName("recentPlay") +
                " FROM " + StringUtil.toSQLName("Game") + " G " +
                " LEFT JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                " ON G." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("game") +
                " LEFT JOIN " + StringUtil.toSQLName("Play") + " P " +
                " ON GPP." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " PPG " +
                " ON PPG." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " WHERE PPG." + StringUtil.toSQLName("GameGroup") + " = ? ";
        query = query + " GROUP BY G." + StringUtil.toSQLName("gameName");
        switch (sortType) {
            case 0:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 1:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " DESC";
                break;
            case 2:
                query = query + " ORDER BY COUNT(P." + StringUtil.toSQLName("id") + ") DESC, MAX(P." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 3:
                query = query + " ORDER BY COUNT(P." + StringUtil.toSQLName("id") + ") ASC, MAX(P." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 4:
                query = query + " ORDER BY MAX(P." + StringUtil.toSQLName("playDate") + ") DESC, G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 5:
                query = query + " ORDER BY MAX(P." + StringUtil.toSQLName("playDate") + ") ASC, G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
        }
        return Game.findWithQuery(Game.class, query, group.getId().toString());
    }

    public static List<Game> findAllGames(String mSearchQuery, int sortType, boolean includeZero){
        String query;
        if (mSearchQuery.contains("'")) {
            mSearchQuery = mSearchQuery.replaceAll("'", "''");
        }
        query = " SELECT G.*, COUNT(P." + StringUtil.toSQLName("id") + ") AS " + StringUtil.toSQLName("playCount") + ", MAX(P." + StringUtil.toSQLName("playDate") + ") AS " + StringUtil.toSQLName("recentPlay") +
                " FROM " + StringUtil.toSQLName("Game") + " G ";
        if (includeZero) {
            query = query + " LEFT JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP ";
        }else{
            query = query + " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP ";
        }
        query = query + " ON G." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("game");
        if (includeZero) {
            query = query + " LEFT JOIN " + StringUtil.toSQLName("Play") + " P ";
        }else{
            query = query + " INNER JOIN " + StringUtil.toSQLName("Play") + " P ";
        }
        query = query + " ON GPP." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id");
        if (!mSearchQuery.equals("")) {
            query = query + " WHERE G." + StringUtil.toSQLName("gameName") + " LIKE '%" + mSearchQuery + "%'";
        }
        query = query + " GROUP BY G." + StringUtil.toSQLName("gameName");
        switch (sortType) {
            case 0:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 1:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " DESC";
                break;
            case 2:
                query = query + " ORDER BY COUNT(P." + StringUtil.toSQLName("id") + ") DESC, MAX(P." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 3:
                query = query + " ORDER BY COUNT(P." + StringUtil.toSQLName("id") + ") ASC, MAX(P." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 4:
                query = query + " ORDER BY MAX(P." + StringUtil.toSQLName("playDate") + ") DESC, G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 5:
                query = query + " ORDER BY MAX(P." + StringUtil.toSQLName("playDate") + ") ASC, G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
        }
        return Game.findWithQuery(Game.class, query);
    }

    public static List<Game> totalTenByTen_GameGroup(GameGroup group, int year){

        String qwoorty = " SELECT G.*, COUNT(G." + StringUtil.toSQLName("gameName") + ") AS " + StringUtil.toSQLName("tbtCount") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " PPGG " +
                " ON P." + StringUtil.toSQLName("id") + " = PPGG." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                " ON P." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("Game") + " G " +
                " ON GPP." + StringUtil.toSQLName("game") + " = G." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("TenByTen") + " TBT " +
                " ON PPGG." + StringUtil.toSQLName("gameGroup") + " = TBT." + StringUtil.toSQLName("gameGroup") +
                " WHERE PPGG. " + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND TBT." + StringUtil.toSQLName("year") + " = " + year +
                " AND TBT." + StringUtil.toSQLName("game") + " = GPP." + StringUtil.toSQLName("game") +
                " AND STRFTIME('%Y', DATETIME(SUBSTR(P." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? " +
                " GROUP BY G." + StringUtil.toSQLName("gameName") +
                " ORDER BY COUNT(G." + StringUtil.toSQLName("gameName") + ") DESC, G." + StringUtil.toSQLName("gameName");
        Log.d("V1", qwoorty);
        return Game.findWithQuery(Game.class,
                qwoorty, group.getId().toString(), year + "");
    }

    public static List<Game> getUnplayedGames(int sortType, boolean showExpansions, int year){
        String query;
        query = " SELECT "+ StringUtil.toSQLName("Game") + ".*, COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") AS " + StringUtil.toSQLName("playCount") + ", MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") AS " + StringUtil.toSQLName("recentPlay") +
                " FROM " + StringUtil.toSQLName("Game") +
                " LEFT JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " LEFT JOIN " + StringUtil.toSQLName("Play") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id");
        if (year > 0){
            query = query + " AND STRFTIME('%Y', DATETIME(SUBSTR(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? ";
        }
        if (!showExpansions) {
            query = query + " and " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                    " WHERE " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0";
        }
        query = query + " GROUP BY " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") +
                " HAVING COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") = 0 ";
        switch (sortType) {
            case 0:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 1:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 2:
                query = query + " order BY COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") DESC, MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 3:
                query = query + " order BY COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") ASC, MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 4:
                query = query + " order BY MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC, " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 5:
                query = query + " order BY MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") ASC, " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
        }
        if (year > 0){
            return Game.findWithQuery(Game.class, query, year+"");
        }else {
            return Game.findWithQuery(Game.class, query);
        }
    }


    public static List<Game> getUnplayedGames_GameGroup(GameGroup group, int sortType, boolean showExpansions, int year){
        String query;
        query = " SELECT "+ StringUtil.toSQLName("Game") + ".*" +
                " FROM " + StringUtil.toSQLName("Game") +
                " WHERE " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") + " NOT IN" +
                " ( " +
                " SELECT "+ StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " FROM " + StringUtil.toSQLName("Game") +
                " LEFT JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " LEFT JOIN " + StringUtil.toSQLName("Play") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " LEFT JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id");
        if (year > 0){
            query = query + " AND STRFTIME('%Y', DATETIME(SUBSTR(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? ";
        }
        query = query + " WHERE " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("GameGroup") + " = ? ";
        query = query + " GROUP BY " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") +
                " HAVING COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") > 0 ";
        query = query + " ) ";
        if (!showExpansions) {
            query = query + " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0";
        }
        switch (sortType) {
            case 0:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 1:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }

        //Log.d("V1", query);
        if (year > 0){
            return Game.findWithQuery(Game.class, query, group.getId().toString(), year+"");
        }else {
            return Game.findWithQuery(Game.class, query, group.getId().toString());
        }
    }

    public static List<Game> getUniqueGames_GameGroup(GameGroup group, int sortType, int year){
        String query;
        query = " SELECT "+ StringUtil.toSQLName("Game") + ".*, COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") AS " + StringUtil.toSQLName("playCount") + ", MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") AS " + StringUtil.toSQLName("recentPlay") +
                " FROM " + StringUtil.toSQLName("Game") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Play") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " and " + StringUtil.toSQLName("GameGroup") + " = ? ";
        if (year > 0){
            query = query + " AND STRFTIME('%Y', DATETIME(SUBSTR(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? ";
        }
         query = query + " GROUP BY " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName");
        switch (sortType) {
            case 0:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 1:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 2:
                query = query + " order BY COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") DESC, MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 3:
                query = query + " order BY COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") ASC, MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 4:
                query = query + " order BY MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC, " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 5:
                query = query + " order BY MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") ASC, " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
        }
        if (year > 0){
            return Game.findWithQuery(Game.class, query, group.getId().toString(), year+"");
        }else {
            return Game.findWithQuery(Game.class, query, group.getId().toString());
        }
    }

    public static List<Game> getUniqueGames(int sortType, int year){
        String query;
        query = " SELECT "+ StringUtil.toSQLName("Game") + ".*, COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") AS " + StringUtil.toSQLName("playCount") + ", MAX(" + StringUtil.toSQLName("Play") +"."+ StringUtil.toSQLName("playDate") + ") AS " + StringUtil.toSQLName("recentPlay") +
                " FROM " + StringUtil.toSQLName("Game") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Play") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 ";
        if (year > 0){
            query = query + " AND STRFTIME('%Y', DATETIME(SUBSTR(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? ";
        }
        query = query + " GROUP BY " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName");
        switch (sortType) {
            case 0:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 1:
                query = query + " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 2:
                query = query + " order BY COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") DESC, MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 3:
                query = query + " order BY COUNT(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + ") ASC, MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC";
                break;
            case 4:
                query = query + " order BY MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") DESC, " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
            case 5:
                query = query + " order BY MAX(" + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + ") ASC, " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
                break;
        }
        if (year > 0){
            return Game.findWithQuery(Game.class, query, year+"");
        }else {
            return Game.findWithQuery(Game.class, query);
        }
    }


    public static List<Game> getBucketList(){
        String query;
        query = " SELECT G.*, COUNT(P." + StringUtil.toSQLName("id") + ") AS " + StringUtil.toSQLName("playCount") + ", MAX(P." + StringUtil.toSQLName("playDate") + ") AS " + StringUtil.toSQLName("recentPlay") +
                " FROM " + StringUtil.toSQLName("Game") + " G " +
                " LEFT JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                " ON G." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("game") +
                " LEFT JOIN " + StringUtil.toSQLName("Play") + " P " +
                " ON GPP." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " WHERE G. " + StringUtil.toSQLName("taggedToPlay") + " != 0" +
                " AND G. " + StringUtil.toSQLName("taggedToPlay") + " != \"\"" +
                " AND G. " + StringUtil.toSQLName("expansionFlag") + " = 0" +
                " GROUP BY G." + StringUtil.toSQLName("gameName") +
                " ORDER BY G." + StringUtil.toSQLName("taggedToPlay") + " DESC";
        return Game.findWithQuery(Game.class,query);
    }
}
