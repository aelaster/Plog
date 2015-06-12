package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Select;

import java.util.Date;
import java.util.List;


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

    public static List listPlaysNewOld(){
        Select dateSort_NewOld = Select.from(Play.class);
        dateSort_NewOld.orderBy(StringUtil.toSQLName("playDate") + " DESC, " + StringUtil.toSQLName("ID") + " DESC");
        return dateSort_NewOld.list();
    }



    public static List<Play> listPlaysNewOld(String mSearchQuery, boolean allowLike){
        String query;
        if (mSearchQuery.contains("'")) {
            mSearchQuery = mSearchQuery.replaceAll("'", "''");
        }
        if (mSearchQuery.equals("")) {
           return listPlaysNewOld();
        }else {
           query = " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                    " FROM " + StringUtil.toSQLName("Play") +
                    " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                    " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                    " INNER JOIN " + StringUtil.toSQLName("Game") +
                    " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                    " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 ";
           if(allowLike){
               query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " LIKE '%" + mSearchQuery + "%'";
           }else {
               query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " = '" + mSearchQuery + "'";
           }
           query = query + " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
           return Play.findWithQuery(Play.class,query);
        }
    }

    public static List<Play> listPlaysNewOld_GameGroup(String gameGroup){
        String query;
        query = " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                " FROM " + StringUtil.toSQLName("Play") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("gameGroup") + " = ?";
        query = query + " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
        return Play.findWithQuery(Play.class,query, gameGroup);

    }
}