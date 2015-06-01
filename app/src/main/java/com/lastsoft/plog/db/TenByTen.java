package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
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

    public static boolean isGroupAdded(GameGroup group, Game game){
        Select getPlayers = Select.from(TenByTen.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("gameGroup")).eq(group.getId()), Condition.prop(StringUtil.toSQLName("game")).eq(game.getId()));
        if (getPlayers.list().isEmpty()){
            return false;
        }else{
            return true;
        }

    }

    public static List<TenByTen> tenByTens_Group(GameGroup group){
        /*Select getPlayers = Select.from(TenByTen.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("gameGroup")).eq(group.getId()));
        return getPlayers.list();*/
        return TenByTen.findWithQuery(TenByTen.class, "Select " + StringUtil.toSQLName("TenByTen") + ".* from " + StringUtil.toSQLName("Game") + "," + StringUtil.toSQLName("TenByTen") +
                " where " + StringUtil.toSQLName("TenByTen") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") + " and " + StringUtil.toSQLName("TenByTen") + "." + StringUtil.toSQLName("gameGroup") + " = ? " +
                " order by " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC", ""+group.getId());
    }

    public static void deleteTenByTen(long gameID) {
        TenByTen.findWithQuery(TenByTen.class, "DELETE FROM " + StringUtil.toSQLName("TenByTen") + " where " + StringUtil.toSQLName("game") + " = ?", gameID + "");
    }
}
