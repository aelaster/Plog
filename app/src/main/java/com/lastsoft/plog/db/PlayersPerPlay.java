package com.lastsoft.plog.db;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class PlayersPerPlay extends SugarRecord<PlayersPerPlay> {

    public Player player;
    public Play play;
    public int score;
    public String color;

    public PlayersPerPlay() {
    }


    public PlayersPerPlay(Player player, Play play, int score, String color) {
        this.player = player;
        this.play = play;
        this.score = score;
        this.color = color;
    }

    public PlayersPerPlay(Player player, Play play, int score) {
        this.player = player;
        this.play = play;
        this.score = score;
        this.color = "";
    }

    public static List<PlayersPerPlay> getPlayers(Play play){
        Select getPlayers = Select.from(PlayersPerPlay.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        return getPlayers.list();
    }

    public static List<PlayersPerPlay> getPlayer(Player player){
        Select getPlayers = Select.from(PlayersPerPlay.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("player")).eq(player.getId()));
        return getPlayers.list();
    }


    public static List<PlayersPerPlay> getPlayers_Winners(Play play){
        Select getPlayers = Select.from(PlayersPerPlay.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        getPlayers.orderBy(StringUtil.toSQLName("score") + " ASC");
        return getPlayers.list();
    }

    public static int getHighScore(Play play){
        List<PlayersPerPlay> queery = PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select * from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ? and " + StringUtil.toSQLName("score") + " = (Select Max(" + StringUtil.toSQLName("score") + ") from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ?)", play.getId().toString(), play.getId().toString());
        return queery.get(0).score;
    }
    public static List<PlayersPerPlay> getWinners(Play play){
        //Select getPlayers = Select.from(PlayersPerPlay.class);
        //getPlayers.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        //return  PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select Max(" + StringUtil.toSQLName("score") + ") from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ?", play.getId().toString());
        return PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select * from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ? and " + StringUtil.toSQLName("score") + " = (Select Max(" + StringUtil.toSQLName("score") + ") from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ?)", play.getId().toString(), play.getId().toString());

        /*
        find(Class<T> type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit)
        I tend to use db.rawQuery(String sql, String[] selectionArgs), so it would be: db.rawQuery("SELECT MAX(price) FROM spendings", null).
         */
    }

}
