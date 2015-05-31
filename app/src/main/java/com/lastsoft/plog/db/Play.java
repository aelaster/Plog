package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Select;

import java.util.Date;
import java.util.List;

/**
 * Created by TheFlash on 5/22/2015.
 */
public class Play extends SugarRecord<Play> {

    public Date playDate;
    public String playNotes;
    public String playPhoto;

    public Play() {
    }

    public Play(Date playDate, String playNotes, String playPhoto) {
        this.playDate = playDate;
        this.playNotes = playNotes;
        this.playPhoto = playPhoto;
    }

    public Play(Date playDate, String playNotes) {
        this.playDate = playDate;
        this.playNotes = playNotes;
        this.playPhoto = "";
    }

    public Play(Date playDate) {
        this.playDate = playDate;
        this.playNotes = "";
        this.playPhoto = "";
    }

    public static List<Play> listPlaysNewOld(){
        Select dateSort_NewOld = Select.from(Play.class);
        dateSort_NewOld.orderBy(StringUtil.toSQLName("playDate") + " DESC, " + StringUtil.toSQLName("ID") + " DESC");

        return dateSort_NewOld.list();
    }
}
