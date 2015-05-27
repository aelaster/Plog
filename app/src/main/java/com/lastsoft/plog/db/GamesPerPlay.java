package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
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

    public static Game getBaseGame(Play play){
        Select getBaseGame = Select.from(GamesPerPlay.class);
        getBaseGame.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("0"));

        GamesPerPlay theBaseGame = (GamesPerPlay) getBaseGame.first();
        return theBaseGame.game;
    }

    public static List<GamesPerPlay> getExpansions(Play play){
        Select getExpansions = Select.from(GamesPerPlay.class);
        getExpansions.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()), Condition.prop(StringUtil.toSQLName("expansionFlag")).eq("1"));
        return getExpansions.list();
    }

    public static List<GamesPerPlay> getGames(Play play){
        Select getGames = Select.from(GamesPerPlay.class);
        getGames.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        return getGames.list();
    }

}
