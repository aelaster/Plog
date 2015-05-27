package com.lastsoft.plog.db;

import com.orm.SugarRecord;

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

}
