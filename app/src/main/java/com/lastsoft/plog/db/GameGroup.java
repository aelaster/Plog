package com.lastsoft.plog.db;

import com.orm.SugarRecord;

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

}
