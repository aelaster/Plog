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
    public int playHighScore;

    public PlayersPerPlay() {
    }


    public PlayersPerPlay(Player player, Play play, int score, String color, int playHighScore) {
        this.player = player;
        this.play = play;
        this.score = score;
        this.color = color;
        this.playHighScore = playHighScore;
    }

    public PlayersPerPlay(Player player, Play play, int score, int playHighScore) {
        this.player = player;
        this.play = play;
        this.score = score;
        this.color = "";
        this.playHighScore = playHighScore;
    }

    public static List<PlayersPerPlay> totalPlays_GameGroup(GameGroup group) {
        /*Log.d("V1", " SELECT "+ StringUtil.toSQLName("PlayersPerPlay") +".* " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") +
                " AND " + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " IN " +
                " (SELECT " + StringUtil.toSQLName("Player") +
                " FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                " WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                " ORDER BY PLAY, PLAYER");*/
        return PlayersPerPlay.findWithQuery(PlayersPerPlay.class,
                " SELECT "+ StringUtil.toSQLName("PlayersPerPlay") +".* " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                        " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") +
                        " AND " + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " IN " +
                        " (SELECT " + StringUtil.toSQLName("Player") +
                        " FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                        " WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                        " ORDER BY PLAY, PLAYER", group.getId().toString(), group.getId().toString());
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
        getPlayers.orderBy(StringUtil.toSQLName("score") + " DESC");
        return getPlayers.list();
    }

    public static int getHighScore(Play play){
        List<PlayersPerPlay> queery = PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select * from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ? and " + StringUtil.toSQLName("score") + " = (Select Max(" + StringUtil.toSQLName("score") + ") from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ?)", play.getId().toString(), play.getId().toString());
        return queery.get(0).score;
    }
    public static List<PlayersPerPlay> getWinners(Play play){
        return PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select * from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ? and " + StringUtil.toSQLName("score") + " = (Select Max(" + StringUtil.toSQLName("score") + ") from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ?)", play.getId().toString(), play.getId().toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Long)) {
            return false;
        }
        Long other = (Long) obj;
        return this.player.getId() == other;
    }

}
