package com.example.tonipagliaro.botchain.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Utente on 15/02/2017.
 */

public class Database extends SQLiteOpenHelper {

    public static final String DB_NAME="bot.db";
    public static final String DB_TABLE="bot_table";
    public static final String ATTRIBUTE_ADDRESS="ADDRESS";
    public static final String ATTRIBUTE_OS="OS";
    public static final String ATTRIBUTE_USERNAME="USERNAME";
    public static final String ATTRIBUTE_USERHOME="USERHOME";
    public static final String ATTRIBUTE_PING_OF_DEATH = "PING_OF_DEATH";
    public static final String ATTRIBUTE_BALANCE = "BALANCE";


    public Database(Context context) {
        super(context, DB_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DB_TABLE + " (ADDRESS TEXT PRIMARY KEY, OS TEXT, USERNAME TEXT, USERHOME TEXT, PING_OF_DEATH TEXT, BALANCE TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXIST "+DB_TABLE);
        onCreate(db);
    }

    public boolean insertData (String address, String os, String username, String userhome, String pingOfDeath, String balance){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(ATTRIBUTE_ADDRESS,address);
        contentValues.put(ATTRIBUTE_OS,os);
        contentValues.put(ATTRIBUTE_USERNAME,username);
        contentValues.put(ATTRIBUTE_USERHOME,userhome);
        contentValues.put(ATTRIBUTE_PING_OF_DEATH, pingOfDeath);
        contentValues.put(ATTRIBUTE_BALANCE, balance);
        long result=db.insert(DB_TABLE,null,contentValues);
        if (result ==-1)
            return false;
        else
            return true;

    }

    public boolean updateOs(String address, String os){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(ATTRIBUTE_ADDRESS,address);
        contentValues.put(ATTRIBUTE_OS,os);
        long result=db.update(DB_TABLE, contentValues, "ADDRESS = ?", new String[]{address});
        if (result ==-1)
            return false;
        else
            return true;
    }

    public boolean updateUsername(String address, String username){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(ATTRIBUTE_ADDRESS,address);
        contentValues.put(ATTRIBUTE_USERNAME, username);
        long result=db.update(DB_TABLE, contentValues, "ADDRESS = ?", new String[]{address});
        if (result ==-1)
            return false;
        else
            return true;
    }

    public boolean updateUserHome(String address, String userhome){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(ATTRIBUTE_ADDRESS,address);
        contentValues.put(ATTRIBUTE_USERHOME,userhome);
        long result=db.update(DB_TABLE, contentValues, "ADDRESS = ?", new String[]{address});
        if (result ==-1)
            return false;
        else
            return true;
    }


    public boolean updatePingOfDeath(String address, String pingOfDeath){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(ATTRIBUTE_ADDRESS,address);
        contentValues.put(ATTRIBUTE_PING_OF_DEATH,pingOfDeath);
        long result=db.update(DB_TABLE, contentValues, "ADDRESS = ?", new String[]{address});
        if (result ==-1)
            return false;
        else
            return true;
    }

    public boolean updateBalance(String address, String balance){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(ATTRIBUTE_ADDRESS,address);
        contentValues.put(ATTRIBUTE_BALANCE,balance);
        long result=db.update(DB_TABLE, contentValues, "ADDRESS = ?", new String[]{address});
        if (result ==-1)
            return false;
        else
            return true;
    }

    public String getOS(String address){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+ DB_TABLE+" WHERE ADDRESS = '"+address +"'",null);
        res.moveToNext();
        Log.d("App", res.getString(1));
        return res.getString(1);

    }
    public String getUsername(String address){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+ DB_TABLE+" WHERE ADDRESS = '"+address +"'" ,null);
        res.moveToNext();
        return res.getString(2);
    }
    public String getUserhome(String address){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+ DB_TABLE+" WHERE ADDRESS = '"+address +"'" ,null);
        res.moveToNext();
        return res.getString(3);
    }
    public String getPingOfDeath(String address){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+ DB_TABLE+" WHERE ADDRESS = '"+address +"'" ,null);
        res.moveToNext();
        return res.getString(4);
    }
    public String getBalance(String address){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor res= db.rawQuery("SELECT * FROM "+ DB_TABLE+" WHERE ADDRESS = '"+address +"'" ,null);
        res.moveToNext();
        return res.getString(5);
    }

}

