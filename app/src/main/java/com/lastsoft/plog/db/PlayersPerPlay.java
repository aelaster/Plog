package com.lastsoft.plog.db;

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

    public static List<PlayersPerPlay> totalPlays_GameGroup(GameGroup group){
        return PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select * from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("player") + " in (Select " + StringUtil.toSQLName("player") + " from " + StringUtil.toSQLName("PlayersPerGameGroup") + " where " + StringUtil.toSQLName("GameGroup") + " = ?) ORDER BY PLAY, PLAYER", group.getId().toString());
        /*return PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select " + StringUtil.toSQLName("PlayersPerPlay") +  ".* from " + StringUtil.toSQLName("PlayersPerGameGroup") + ", " + StringUtil.toSQLName("PlayersPerPlay") + " where "
                + StringUtil.toSQLName("PlayersPerGameGroup") + "." + StringUtil.toSQLName("GameGroup") + "= ? and "
                + StringUtil.toSQLName("PlayersPerGameGroup") + "." + StringUtil.toSQLName("Player") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player"), group.getId()+"");
                */
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

    public static List<PlayersPerPlay> totalPlays(Player player){
        Select getPlayers = Select.from(PlayersPerPlay.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("player")).eq(player.getId()));
        return getPlayers.list();
    }

    public static int getScoreByPlayer(Player player, Play play){
        Select getPlayers = Select.from(PlayersPerPlay.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("player")).eq(player.getId()), Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        List<PlayersPerPlay> output =  getPlayers.list();
        return output.get(0).score;
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
        //Select getPlayers = Select.from(PlayersPerPlay.class);
        //getPlayers.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        //return  PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select Max(" + StringUtil.toSQLName("score") + ") from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ?", play.getId().toString());
        return PlayersPerPlay.findWithQuery(PlayersPerPlay.class, "Select * from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ? and " + StringUtil.toSQLName("score") + " = (Select Max(" + StringUtil.toSQLName("score") + ") from " + StringUtil.toSQLName("PlayersPerPlay") + " where " + StringUtil.toSQLName("play") + " = ?)", play.getId().toString(), play.getId().toString());
    }

}
