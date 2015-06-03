package com.lastsoft.plog.db;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.SugarTransactionHelper;

import java.util.Date;
import java.util.List;

public class TenByTen_Stats extends SugarRecord<TenByTen_Stats> {

    public Play play;
    public Player player;
    public Date playDate;

    public TenByTen_Stats() {
    }

    public TenByTen_Stats(Play play, Player player, Date playDate) {
        this.play = play;
        this.player = player;
        this.playDate = playDate;
    }

    public static List<TenByTen_Stats> getUniquePlays_GameGroup(String gameName, GameGroup group){
        return TenByTen_Stats.findWithQuery(TenByTen_Stats.class,
                " SELECT * " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Play") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " = ? " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " and " +
                StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " in " +
                "(SELECT " + StringUtil.toSQLName("player") +
                " FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                " WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)", gameName, group.getId().toString());
    }
}
