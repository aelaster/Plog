package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.SugarTransactionHelper;

import java.util.Date;
import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
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
                "Select " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                        ", " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") +
                        ", " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") +
                        " from " + StringUtil.toSQLName("PlayersPerPlay") +
                        ", " + StringUtil.toSQLName("GamesPerPlay") +
                        ", " + StringUtil.toSQLName("Game") +
                        ", " + StringUtil.toSQLName("Play") +
                        " where " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " = ? and " +
                        StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " and " +
                        StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " and " +
                        StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " and " +
                        StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " in " +
                        "(Select " + StringUtil.toSQLName("player") +
                        " from " + StringUtil.toSQLName("PlayersPerGameGroup") +
                        " where " + StringUtil.toSQLName("GameGroup") + " = ?)", gameName, group.getId().toString());
    }
}
