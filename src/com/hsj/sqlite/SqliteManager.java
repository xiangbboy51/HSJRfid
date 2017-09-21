package com.hsj.sqlite;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xiang on 2016/9/6.
 */
public class SqliteManager extends SQLiteOpenHelper {
    public SqliteManager(Context context) {
        super(context, "rfidTag.db", null, 1);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        //创建数据库
        String sql = "create table if not exists rfid(id integer primary key," +
                "number varchar(50) not null," +
                "epcData varchar(50) not null)";
        //执行建表语句
        db.execSQL(sql);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    
    
    }
    
    
    
   
}
