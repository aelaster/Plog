package com.lastsoft.plog.db;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class GamesPerPlay extends SugarRecord<GamesPerPlay> {

    public Play play;
    public Game game;
    public boolean expansionFlag;

    public GamesPerPlay() {
    }

    public GamesPerPlay(Play play, Game game, boolean expansionFlag) {
        this.play = play;
        this.game = game;
        this.expansionFlag = expansionFlag;
    }

    public static int getUniquePlays_GameGroup(GameGroup group){
        Log.d("V1", "Select * " +
                " from " + StringUtil.toSQLName("GamesPerPlay") +
                " where " + StringUtil.toSQLName("expansionFlag") + " = 0 and " + StringUtil.toSQLName("play") +
                " in (Select " + StringUtil.toSQLName("play") +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("player") +
                " in (Select " + StringUtil.toSQLName("player") +
                " from " + StringUtil.toSQLName("PlayersPerGameGroup") +
                " where " + StringUtil.toSQLName("GameGroup") + " = ?))" +
                " group by " + StringUtil.toSQLName("game"));
        List<GamesPerPlay> queery = GamesPerPlay.findWithQuery(GamesPerPlay.class,
                "Select * " +
                " from " + StringUtil.toSQLName("GamesPerPlay") +
                " where " + StringUtil.toSQLName("expansionFlag") + " = 0 and " + StringUtil.toSQLName("play") +
                " in (Select " + StringUtil.toSQLName("play") +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("player") +
                " in (Select " + StringUtil.toSQLName("player") +
                " from " + StringUtil.toSQLName("PlayersPerGameGroup") +
                " where " + StringUtil.toSQLName("GameGroup") + " = ?))" +
                " group by " + StringUtil.toSQLName("game"), group.getId().toString());
        return queery.size();
    }

    public static int getUniquePlays(){
        List<GamesPerPlay> queery = GamesPerPlay.findWithQuery(GamesPerPlay.class, "Select * from " + StringUtil.toSQLName("GamesPerPlay") + " where " + StringUtil.toSQLName("expansionFlag") + " = 0 order by " + StringUtil.toSQLName("game"));
        return queery.size();
    }

    public static Game getBaseGame(Play play){
        Log.d("V1", "playID = " + play.getId());
        Select getBaseGame = Select.from(GamesPerPlay.class);
        getBaseGame.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("0"));

        GamesPerPlay theBaseGame = (GamesPerPlay) getBaseGame.first();
        return theBaseGame.game;
    }

    public static List getExpansions(Play play){
        Select getExpansions = Select.from(GamesPerPlay.class);
        getExpansions.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("1"));
        return getExpansions.list();
    }

    public static List getGames(Play play){
        Select getGames = Select.from(GamesPerPlay.class);
        getGames.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        return getGames.list();
    }

    public static boolean doesExpansionExist(Play play, Game testExpansion){
        Select getExpansion = Select.from(GamesPerPlay.class);
        getExpansion.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()), Condition.prop(StringUtil.toSQLName("game")).eq(testExpansion.getId()));
        List tester = getExpansion.list();
        return (!tester.isEmpty());
    }

    public static boolean hasGameBeenPlayed(Game game){
        Select hasGameBeenPlayed = Select.from(GamesPerPlay.class);
        hasGameBeenPlayed.where(Condition.prop(StringUtil.toSQLName("game")).eq(game.getId()));
        List tester = hasGameBeenPlayed.list();
        return (!tester.isEmpty());
    }

}
