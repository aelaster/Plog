package com.lastsoft.plog.db;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class PlaysPerGameGroup extends SugarRecord<PlaysPerGameGroup> {

    public Play play;
    public GameGroup gameGroup;

    public PlaysPerGameGroup() {
    }

    public PlaysPerGameGroup(Play play, GameGroup gameGroup) {
        this.play = play;
        this.gameGroup = gameGroup;
    }

    public static List getPlays(Play play){
        Select getGames = Select.from(PlaysPerGameGroup.class);
        getGames.where(Condition.prop(StringUtil.toSQLName("play")).eq(play.getId()));
        return getGames.list();
    }

    public static List getPlays(GameGroup group){
        Select getGames = Select.from(PlaysPerGameGroup.class);
        getGames.where(Condition.prop(StringUtil.toSQLName("gameGroup")).eq(group.getId()));
        return getGames.list();
    }
}
