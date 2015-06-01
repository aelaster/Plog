package com.lastsoft.plog.db;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class Game extends SugarRecord<Game> {

    public String gameName;
    public String gameBGGID;
    public String gameImage;
    public String gameThumb;
    public boolean expansionFlag;

    public Game() {
    }

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
        return (Game.find(Game.class, StringUtil.toSQLName("gameName") + " = ?", name)).get(0);
    }

    public static Game findGameByName_NoCase(String name) {
        List<Game> queery = Game.findWithQuery(Game.class, "Select * from " + StringUtil.toSQLName("Game") + " where " + StringUtil.toSQLName("gameName") + " = ? COLLATE NOCASE", name);
        if (queery.size() > 0) {
            return queery.get(0);
        } else {
            return null;
        }
    }

    public static List<Game> findExpansions(){
        //return Game.find(Game.class, StringUtil.toSQLName("expansionFlag") + " = 1");
        Select findExpansions = Select.from(Game.class);
        findExpansions.where(Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("1"));
        findExpansions.orderBy(StringUtil.toSQLName("gameName") + " ASC");
        return findExpansions.list();
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
        //Log.d("V1", name);
        Select expansionsFor = Select.from(Game.class);
        expansionsFor.where(Condition.prop(StringUtil.toSQLName("gameName")).like("%" + name + "%"), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("1"));
        expansionsFor.orderBy(StringUtil.toSQLName("gameName") + " ASC");
        return expansionsFor.list();
    }
}
