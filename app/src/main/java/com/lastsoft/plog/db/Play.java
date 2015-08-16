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
    public String bggPlayID;

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



    public static List<Play> listPlaysNewOld(String mSearchQuery, boolean allowLike, boolean allowExpansions){
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
                    " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id");
            if (!allowExpansions){
                query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 ";
            }
           if(allowLike){
               query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " LIKE '%" + mSearchQuery + "%'";
           }else {
               query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " = '" + mSearchQuery + "'";
           }
           query = query + " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
           return Play.findWithQuery(Play.class,query);
        }
    }

    public static List<Play> listPlaysNewOld_GameGroup(String gameGroup, String mSearchQuery, boolean allowLike, boolean allowExpansions){
        String query;
        if (mSearchQuery.contains("'")) {
            mSearchQuery = mSearchQuery.replaceAll("'", "''");
        }
        if (mSearchQuery.equals("")) {
            return listPlaysNewOld_GameGroup(gameGroup);
        }else {
            query = " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                    " FROM " + StringUtil.toSQLName("Play") +
                    " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                    " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                    " INNER JOIN " + StringUtil.toSQLName("Game") +
                    " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id");
            if (!allowExpansions){
                query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 ";
            }
            if(allowLike){
                query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " LIKE '%" + mSearchQuery + "%'";
            }else {
                query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " = '" + mSearchQuery + "'";
            }
            query = query + " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                    " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") +
                    " and " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("gameGroup") + " = ?";
            query = query + " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
            return Play.findWithQuery(Play.class,query, gameGroup);
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
        return Play.findWithQuery(Play.class, query, gameGroup);

    }

    public static List<Play> gameTenByTen_GameGroup(GameGroup group, Game game, int year){
        return Play.findWithQuery(Play.class,
                " SELECT P.* " +
                        " FROM " + StringUtil.toSQLName("Play") + " P " +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " PPGG " +
                        " ON P." + StringUtil.toSQLName("id") + " = PPGG." + StringUtil.toSQLName("play") +
                        " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                        " ON P." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("play") +
                        " INNER JOIN " + StringUtil.toSQLName("TenByTen") + " TBT " +
                        " ON PPGG." + StringUtil.toSQLName("gameGroup") + " = TBT." + StringUtil.toSQLName("gameGroup") +
                        " INNER JOIN " + StringUtil.toSQLName("Game") + " G " +
                        " ON GPP." + StringUtil.toSQLName("game") + " = G." + StringUtil.toSQLName("id") +
                        " WHERE PPGG. " + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND TBT." + StringUtil.toSQLName("game") + " = GPP." + StringUtil.toSQLName("game") +
                        " AND G." + StringUtil.toSQLName("id") + " = ? " +
                        " AND STRFTIME('%Y', DATETIME(SUBSTR(P." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? " +
                        " GROUP BY P." + StringUtil.toSQLName("id") +
                        " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC, P." + StringUtil.toSQLName("id") + " DESC", group.getId().toString(), game.getId().toString(), year + "");
    }

    public static List<Play> totalPlays_TenByTen_GameGroup(GameGroup group, int year){
        return Play.findWithQuery(Play.class,
                " SELECT P.* " +
                        " FROM " + StringUtil.toSQLName("Play") + " P " +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " PPGG " +
                        " ON P." + StringUtil.toSQLName("id") + " = PPGG." + StringUtil.toSQLName("play") +
                        " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                        " ON P." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("play") +
                        " INNER JOIN " + StringUtil.toSQLName("TenByTen") + " TBT " +
                        " ON PPGG." + StringUtil.toSQLName("gameGroup") + " = TBT." + StringUtil.toSQLName("gameGroup") +
                        " INNER JOIN " + StringUtil.toSQLName("Game") + " G " +
                        " ON GPP." + StringUtil.toSQLName("game") + " = G." + StringUtil.toSQLName("id") +
                        " WHERE PPGG. " + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND TBT." + StringUtil.toSQLName("game") + " = GPP." + StringUtil.toSQLName("game") +
                        " AND STRFTIME('%Y', DATETIME(SUBSTR(P." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? " +
                        " GROUP BY P." + StringUtil.toSQLName("id") +
                        " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC, P." + StringUtil.toSQLName("id") + " DESC", group.getId().toString(), year + "");
    }

    public static List<Play> totalWins_GameGroup_Player(GameGroup group, Player player) {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                        " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") +
                        " AND " + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? " +
                        " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("score") + " >= " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("playHighScore") +
                        " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", group.getId().toString(), player.getId().toString());
    }

    public static List<Play> totalPlays_Player(Player player) {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                        " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? " +
                        " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", player.getId().toString());
    }

    public static List<Play> totalWins_Player(Player player) {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                        " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? " +
                        " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("score") + " >= " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("playHighScore") +
                        " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", player.getId().toString());
    }

    public static List<Play> totalAsteriskWins_GameGroup_Player(GameGroup group, Player player) {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                        " WHERE P." + StringUtil.toSQLName("player") + " = ? " +
                        " AND P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                        " AND P." + StringUtil.toSQLName("score") + " > 0 " +
                        " AND P." + StringUtil.toSQLName("score") + " = " +
                        " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " B " +
                        " ON A." + StringUtil.toSQLName("play") + " = B." + StringUtil.toSQLName("play") +
                        " WHERE B." + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " AND A." + StringUtil.toSQLName("player") + " in " +
                        " (SELECT " + StringUtil.toSQLName("player") +
                        "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                        "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                        " )" +
                        " GROUP BY P." + StringUtil.toSQLName("play") +
                        " HAVING " +
                        " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                        "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " R " +
                        " ON Q." + StringUtil.toSQLName("play") + " = R." + StringUtil.toSQLName("play") +
                        " WHERE R." + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                        ") = 1" +
                        " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", player.getId().toString(), group.getId().toString(), group.getId().toString(), group.getId().toString());
    }

    public static List<Play> totalAsteriskWins_Player(Player player) {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                        " WHERE P." + StringUtil.toSQLName("player") + " = ? " +
                        " AND P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                        " AND P." + StringUtil.toSQLName("score") + " > 0 " +
                        " AND P." + StringUtil.toSQLName("score") + " = " +
                        " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                        " WHERE A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " )GROUP BY P." + StringUtil.toSQLName("play") +
                        " HAVING " +
                        " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                        "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                        " WHERE Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                        ") = 1" +
                        " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", player.getId().toString());
    }

    public static List<Play> totalGroupLosses(GameGroup group) {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                        " WHERE P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                        /*" AND P." + StringUtil.toSQLName("score") + " = " +
                        " (SELECT MIN(A." + StringUtil.toSQLName("score") + ") " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                        " WHERE A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") + " ) " +*/
                        " AND P." + StringUtil.toSQLName("player") + " in " +
                        " (SELECT " + StringUtil.toSQLName("player") +
                        "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                        "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                        " GROUP BY P." + StringUtil.toSQLName("play") +
                        " HAVING " +
                        " (SELECT COUNT(*) " +
                        " FROM " +
                        " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                        "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " R " +
                        " ON Q." + StringUtil.toSQLName("play") + " = R." + StringUtil.toSQLName("play") +
                        " INNER JOIN " + StringUtil.toSQLName("PlayersPerGameGroup") + " S " +
                        " ON Q." + StringUtil.toSQLName("player") + " = S." + StringUtil.toSQLName("player") +
                        " WHERE S." + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                        " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                        ") = " +
                        " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                        "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                        "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                        " ORDER BY " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", group.getId().toString(), group.getId().toString(), group.getId().toString());
    }

    public static List<Play> totalGroupLosses() {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                        " WHERE P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                        " GROUP BY P." + StringUtil.toSQLName("play") +
                        " HAVING " +
                        " (SELECT COUNT(*) " +
                        " FROM " +
                        " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                        " WHERE Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                        " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                        ") = " +
                        " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                        "  FROM " + StringUtil.toSQLName("Player") +
                        " ) ORDER BY " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC");
    }

    public static List<Play> totalSharedWins(GameGroup group) {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                        " WHERE P." + StringUtil.toSQLName("score") + " != 0" +
                        " AND P." + StringUtil.toSQLName("score") + " = " +
                        " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                        " WHERE A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") + " ) " +
                        " AND P." + StringUtil.toSQLName("player") + " in " +
                        " (SELECT " + StringUtil.toSQLName("player") +
                        "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                        "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                        " GROUP BY P." + StringUtil.toSQLName("play") +
                        " HAVING " +
                        " (SELECT COUNT(*) " +
                        " FROM " +
                        " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                        "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                        " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " R " +
                        " ON Q." + StringUtil.toSQLName("play") + " = R." + StringUtil.toSQLName("play") +
                        " INNER JOIN " + StringUtil.toSQLName("PlayersPerGameGroup") + " S " +
                        " ON Q." + StringUtil.toSQLName("player") + " = S." + StringUtil.toSQLName("player") +
                        " WHERE S." + StringUtil.toSQLName("GameGroup") + " = ?" +
                        " AND Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                        " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                        ") = " +
                        " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                        "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                        "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                        " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC", group.getId().toString(), group.getId().toString(), group.getId().toString());
    }

    public static List<Play> totalSharedWins() {
        return Play.findWithQuery(Play.class,
                " SELECT "+ StringUtil.toSQLName("Play") +".* " +
                        " FROM " + StringUtil.toSQLName("Play") +
                        " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                        " ON " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                        " WHERE P." + StringUtil.toSQLName("score") + " != 0" +
                        " AND P." + StringUtil.toSQLName("score") + " = " +
                        " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                        " WHERE A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") + " ) " +
                        " GROUP BY P." + StringUtil.toSQLName("play") +
                        " HAVING " +
                        " (SELECT COUNT(*) " +
                        " FROM " +
                        " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                        " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                        " WHERE Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                        " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                        " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                        ") = " +
                        " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                        "  FROM " + StringUtil.toSQLName("Player") +
                        "  )" +
                        " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC");
    }
}