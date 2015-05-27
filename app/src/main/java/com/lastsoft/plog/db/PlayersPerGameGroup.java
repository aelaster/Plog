package com.lastsoft.plog.db;

import com.orm.SugarRecord;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class PlayersPerGameGroup extends SugarRecord<PlayersPerGameGroup> {

    public Player player;
    public GameGroup gameGroup;

    public PlayersPerGameGroup() {
    }

    public PlayersPerGameGroup(Player player, GameGroup gameGroup) {
        this.player = player;
        this.gameGroup = gameGroup;
    }

}
