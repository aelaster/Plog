package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class GameGroup extends SugarRecord<GameGroup> {

    public String groupName;

    public GameGroup() {
    }

    public GameGroup(String groupName) {
        this.groupName = groupName;
    }

    public static List<Player> getGroupPlayers(GameGroup group){
        //Log.d("V1", "Select * from " + StringUtil.toSQLName("Player") + " where " + StringUtil.toSQLName("id") + " in (Select " + StringUtil.toSQLName("player") + " from " + StringUtil.toSQLName("PlayersPerGameGroup") + " where " + StringUtil.toSQLName("GameGroup") + " = ?)");
        return Player.findWithQuery(Player.class, "Select * from " + StringUtil.toSQLName("Player") + " where " + StringUtil.toSQLName("id") + " in (Select " + StringUtil.toSQLName("player") + " from " + StringUtil.toSQLName("PlayersPerGameGroup") + " where " + StringUtil.toSQLName("GameGroup") + " = ?) ORDER BY " + StringUtil.toSQLName("playerName"), group.getId().toString());
    }

}
