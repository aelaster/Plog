package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class GamesPerPlay extends SugarRecord<GamesPerPlay> {

    public Play play;
    public Game game;
    public boolean expansionFlag;
    public String bggPlayId;

    public GamesPerPlay() {
    }

    public GamesPerPlay(Play play, Game game, boolean expansionFlag) {
        this.play = play;
        this.game = game;
        this.expansionFlag = expansionFlag;
    }

    public GamesPerPlay(Play play, Game game, boolean expansionFlag, String bggPlayId) {
        this.play = play;
        this.game = game;
        this.expansionFlag = expansionFlag;
        this.bggPlayId = bggPlayId;
    }


    public static Game getBaseGame(Play play){
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

    public static boolean hasGameBeenPlayed(Game game, Player player){
        List<GamesPerPlay> query = GamesPerPlay.findWithQuery(GamesPerPlay.class,
                " SELECT "+ StringUtil.toSQLName("GamesPerPlay") + ".*" +
                        " FROM " + StringUtil.toSQLName("GamesPerPlay") +
                        " INNER JOIN " + StringUtil.toSQLName("PlayersPerPlay") +
                        " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") +
                        " and " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = ? " +
                        " and " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? ", game.getId().toString(), player.getId().toString());
        return query.size() > 1;
    }
}
