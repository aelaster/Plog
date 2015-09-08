package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class GameGroup extends SugarRecord<GameGroup> {

    public String groupName;
    public int totalPlays;
    public int uniqueGames;

    public GameGroup() {
    }

    public GameGroup(String groupName) {
        this.groupName = groupName;
    }

    public static List<Player> getGroupPlayers(GameGroup group){
        //Log.d("V1", "Select * from " + StringUtil.toSQLName("Player") + " where " + StringUtil.toSQLName("id") + " in (Select " + StringUtil.toSQLName("player") + " from " + StringUtil.toSQLName("PlayersPerGameGroup") + " where " + StringUtil.toSQLName("GameGroup") + " = ?)");
        return Player.findWithQuery(Player.class, "Select * from " + StringUtil.toSQLName("Player") + " where " + StringUtil.toSQLName("id") + " in (Select " + StringUtil.toSQLName("player") + " from " + StringUtil.toSQLName("PlayersPerGameGroup") + " where " + StringUtil.toSQLName("GameGroup") + " = ?) ORDER BY " + StringUtil.toSQLName("playerName"), group.getId().toString());
    }

    public static List<GameGroup> listAll_AZ(boolean skipAZ){
        /*Select findGroups = Select.from(GameGroup.class);
        findGroups.orderBy(StringUtil.toSQLName("groupName") + " ASC");
        return findGroups.list();*/
        String query;
        query = " SELECT " + StringUtil.toSQLName("GameGroup") + ".*, COUNT("+ StringUtil.toSQLName("Game") +"." + StringUtil.toSQLName("id") + ") as " + StringUtil.toSQLName("totalPlays") + ", COUNT(DISTINCT "+ StringUtil.toSQLName("Game") +"." + StringUtil.toSQLName("id") + ") as " + StringUtil.toSQLName("uniqueGames") +
                " FROM " + StringUtil.toSQLName("GameGroup") +
                " LEFT JOIN "+ StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("gameGroup") + " = " + StringUtil.toSQLName("GameGroup") + "." + StringUtil.toSQLName("id") +
                " LEFT JOIN "+ StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("Play") + " = " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("Play") +
                " LEFT JOIN "+ StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " AND " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " GROUP BY " + StringUtil.toSQLName("GameGroup") + "." + StringUtil.toSQLName("id");
        if (!skipAZ) {
            query = query + " ORDER BY " + StringUtil.toSQLName("GameGroup") + "." + StringUtil.toSQLName("groupName") + " ASC";
        }
        return GameGroup.findWithQuery(GameGroup.class, query);
    }


}
