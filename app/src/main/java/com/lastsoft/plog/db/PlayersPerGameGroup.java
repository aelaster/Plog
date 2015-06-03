package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class PlayersPerGameGroup extends SugarRecord<PlayersPerGameGroup> {

    public Player player;
    public GameGroup gameGroup;

    public PlayersPerGameGroup() {
    }

    public PlayersPerGameGroup(Player player, GameGroup gameGroup) {
        this.player = player;
        this.gameGroup = gameGroup;
    }

    public static List getPlayer(Player player){
        Select getPlayers = Select.from(PlayersPerGameGroup.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("player")).eq(player.getId()));
        return getPlayers.list();
    }

    public static List getPlayers(GameGroup group){
        Select getPlayers = Select.from(PlayersPerGameGroup.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("gameGroup")).eq(group.getId()));
        return getPlayers.list();
    }

    public static PlayersPerGameGroup getPlayer(Player player, GameGroup group){
        Select getPlayers = Select.from(PlayersPerGameGroup.class);
        getPlayers.where(Condition.prop(StringUtil.toSQLName("player")).eq(player.getId()), Condition.prop(StringUtil.toSQLName("gameGroup")).eq(group.getId()));
        return (PlayersPerGameGroup) getPlayers.first();
    }

}
