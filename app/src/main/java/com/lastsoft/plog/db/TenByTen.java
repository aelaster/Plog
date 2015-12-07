package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class TenByTen extends SugarRecord<TenByTen> {

    public Game game;
    public GameGroup gameGroup;
    public int year;

    public TenByTen() {
    }

    public TenByTen(Game game, GameGroup gameGroup, int year) {
        this.game = game;
        this.gameGroup = gameGroup;
        this.year = year;
    }

    public static boolean isGroupAdded(GameGroup group, Game game, int year){
        Select getPlayers = Select.from(TenByTen.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("year")).eq(year), Condition.prop(StringUtil.toSQLName("gameGroup")).eq(group.getId()), Condition.prop(StringUtil.toSQLName("game")).eq(game.getId()));
        return (!getPlayers.list().isEmpty());

    }

    public static List<TenByTen> tenByTens_Group(GameGroup group, int year){
        return TenByTen.findWithQuery(TenByTen.class,
                " SELECT * " +
                " FROM " + StringUtil.toSQLName("TenByTen") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("TenByTen") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("TenByTen") + "." + StringUtil.toSQLName("gameGroup") + " = ? " +
                        " and " + StringUtil.toSQLName("TenByTen") + "." + StringUtil.toSQLName("year") + " = ? " +
                " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC ", ""+group.getId(), ""+year);
    }

    public static void deleteTenByTen(long gameID, int year) {
        TenByTen.findWithQuery(TenByTen.class, "DELETE FROM " + StringUtil.toSQLName("TenByTen") + " where " + StringUtil.toSQLName("game") + " = ?" +
                " and " + StringUtil.toSQLName("TenByTen") + "." + StringUtil.toSQLName("year") + " = ? ", gameID + "", year + "");
    }
}
