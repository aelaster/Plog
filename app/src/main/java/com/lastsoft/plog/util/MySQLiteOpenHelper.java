package com.lastsoft.plog.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by TheFlash on 11/4/2015.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper{
    public static String DB_FILEPATH = "/data/data/com.lastsoft.plog/databases/SRX.db";

    private static final String DATABASE_NAME = "SRX.db";
    private static final int DATABASE_VERSION = 57;

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     * */
    public boolean importDatabase(String dbPath) throws IOException {

        // Close the SQLiteOpenHelper so it will commit the created empty
        // database to internal storage.
        close();

        //Log.d("V1", "new db = " + dbPath);
        //Log.d("V1", "old db = " + DB_FILEPATH);
        File newDb = new File(dbPath);
        File oldDb = new File(DB_FILEPATH);
        if (newDb.exists()) {
            if(oldDb.exists()){

            }
            else{
                oldDb.getParentFile().mkdirs();
            }
            FileUtils.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
            // Access the copied database so SQLiteHelper will cache it and mark
            // it as created.
            getWritableDatabase().close();
            return true;
        }
        return false;
    }
}
