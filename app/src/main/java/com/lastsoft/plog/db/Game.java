package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class Game extends SugarRecord<Game> {

    public String gameName;
    public String gameBGGID;
    public String gameImage;
    public String gameThumb;
    public String gameBoxImage;
    public boolean expansionFlag;
    public int tbtCount;
    public int taggedToPlay;

    public Game(){ }

    public Game(String gameName, String gameBGGID, String gameImage, String gameThumb, boolean expansionFlag) {
        this.gameName = gameName;
        this.gameBGGID = gameBGGID;
        this.gameImage = gameImage;
        this.gameThumb = gameThumb;
        this.expansionFlag = expansionFlag;
    }


    public Game(String gameName, String gameBGGID, String gameThumb, boolean expansionFlag) {
        this.gameName = gameName;
        this.gameBGGID = gameBGGID;
        this.gameImage = null;
        this.gameThumb = gameThumb;
        this.expansionFlag = expansionFlag;
    }

    public Game(String gameName, String gameBGGID, String gameThumb) {
        this.gameName = gameName;
        this.gameBGGID = gameBGGID;
        this.gameImage = null;
        this.gameThumb = gameThumb;
        this.expansionFlag = false;
    }

    public Game(String gameName, String gameThumb) {
        this.gameName = gameName;
        this.gameBGGID = "";
        this.gameImage = null;
        this.gameThumb = gameThumb;
        this.expansionFlag = false;
    }

    public Game(String gameName, String gameThumb, boolean expansionFlag) {
        this.gameName = gameName;
        this.gameBGGID = "";
        this.gameImage = null;
        this.gameThumb = gameThumb;
        this.expansionFlag = expansionFlag;
    }

    public Game(String gameName) {
        this.gameName = gameName;
        this.gameBGGID = "";
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

    public static Game findGameByName_NoCase(String name) {
        List<Game> queery = Game.findWithQuery(Game.class, "Select * from " + StringUtil.toSQLName("Game") + " where " + StringUtil.toSQLName("gameName") + " = ? COLLATE NOCASE", name);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static List<Game> findBaseGames(String mSearchQuery){
        //return Game.find(Game.class, StringUtil.toSQLName("expansionFlag") + " = 0");
        if (mSearchQuery.contains("'")) {
            mSearchQuery = mSearchQuery.replaceAll("'", "''");
        }
        Select findBaseGames = Select.from(Game.class);
        if (!mSearchQuery.equals("")) {
            findBaseGames.where(Condition.prop(StringUtil.toSQLName("gameName")).like("%" + mSearchQuery + "%"), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("0"));
        }else {
            findBaseGames.where(Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("0"));
        }
        findBaseGames.orderBy(StringUtil.toSQLName("gameName") + " ASC");
        return findBaseGames.list();
    }

    public static List<Game> findExpansionsFor(String name) throws UnsupportedEncodingException {
        if (name.contains("'")) {
            name = name.replaceAll("'", "''");
        }
        Select expansionsFor = Select.from(Game.class);
        //expansionsFor.where(Condition.prop(StringUtil.toSQLName("gameName")).like("%" + name + "%"), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("1"));
        //starts with
        expansionsFor.where(Condition.prop(StringUtil.toSQLName("gameName")).like(name + "%"), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("1"));
        expansionsFor.orderBy(StringUtil.toSQLName("gameName") + " ASC");
        return expansionsFor.list();
    }

    public static List<Game> totalTenByTen_GameGroup(GameGroup group, int year){
        return Game.findWithQuery(Game.class,
                " SELECT G.*, COUNT(G." + StringUtil.toSQLName("gameName") + ") AS " + StringUtil.toSQLName("tbtCount") +
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
                        " AND TBT." + StringUtil.toSQLName("game") + " = GPP." + StringUtil.toSQLName("game") +
                        " AND STRFTIME('%Y', DATETIME(SUBSTR(P." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? " +
                        " GROUP BY G." + StringUtil.toSQLName("gameName") +
                        " ORDER BY COUNT(G." + StringUtil.toSQLName("gameName") + ") DESC, G." + StringUtil.toSQLName("gameName"), group.getId().toString(), year + "");
    }


    public static List<Game> getUniqueGames_GameGroup(GameGroup group){
        return Game.findWithQuery(Game.class,
                " SELECT "+ StringUtil.toSQLName("Game") + ".*" +
                        " FROM " + StringUtil.toSQLName("Game") +
                        " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                        " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                        " INNER JOIN " + StringUtil.toSQLName("Play") +
                        " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                        " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                        " and " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                        " and " + StringUtil.toSQLName("GameGroup") + " = ? group by " + StringUtil.toSQLName("game") +
                        " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", group.getId().toString());
    }

    public static List<Game> isFirstPlay(GameGroup group){
        return Game.findWithQuery(Game.class,
                " SELECT "+ StringUtil.toSQLName("Game") + ".*" +
                        " FROM " + StringUtil.toSQLName("Game") +
                        " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                        " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                        " INNER JOIN " + StringUtil.toSQLName("Play") +
                        " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                        " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                        " and " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                        " and " + StringUtil.toSQLName("GameGroup") + " = ? group by " + StringUtil.toSQLName("game") +
                        " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", group.getId().toString());
    }

    public static List<Game> getBucketList(){
        //return Game.find(Game.class, StringUtil.toSQLName("expansionFlag") + " = 0");
        Select findBaseGames = Select.from(Game.class);
        findBaseGames.where(Condition.prop(StringUtil.toSQLName("taggedToPlay")).notEq("0"), Condition.prop(StringUtil.toSQLName("taggedToPlay")).notEq(""), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("0"));
        findBaseGames.orderBy(StringUtil.toSQLName("taggedToPlay") + " ASC");
        findBaseGames.orderBy(StringUtil.toSQLName("gameName") + " ASC");
        return findBaseGames.list();
    }
}
