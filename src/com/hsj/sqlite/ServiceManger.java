package com.hsj.sqlite;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import com.hsj.entity.EntityTag;
import com.invengo.sample.EPCEntity;

/**
 * Created by Administrator on 2016/9/13.
 */
public class ServiceManger {
    private SqliteManager mysqlite;

    //构造方法
    public ServiceManger(Context context) {
        mysqlite = new SqliteManager(context);
    }

    //添加信息
    public void addEPCEntity(EntityTag item) {
        //可写sql
        SQLiteDatabase sqLiteDatabase = mysqlite.getWritableDatabase();
        // String addsql = "insert into user(username,password) values(?,?)";
        //sqLiteDatabase.execSQL(addsql, new String[]{user.getUsername(), user.getPassword()});
        //构建游标
        ContentValues cv = new ContentValues();
        cv.put("number", item.getNumber());
        cv.put("epcData", item.getEpcData());
        sqLiteDatabase.insert("rfid", null, cv);
    }

    //删除实体
    public void delEPCEntity(String number) {
        SQLiteDatabase sqLiteDatabase = mysqlite.getWritableDatabase();
        // String sql = "delete from user where number=?";
        // sqLiteDatabase.execSQL(sql, new Object[]{id});
        sqLiteDatabase.delete("rfid", "number=?", new String[]{number});
    }
    
  //删除所有数据
    public void delEPCEntityAll() {
        SQLiteDatabase sqLiteDatabase = mysqlite.getWritableDatabase();
        // String sql = "delete from user where id=?";
        // sqLiteDatabase.execSQL(sql, new Object[]{id});
        sqLiteDatabase.delete("rfid", null, null);
    }

    //查询所有的表格中的标签
    public List<EntityTag> queryEPCEntity(String number) {
        List<EntityTag> items = new ArrayList<EntityTag>();
        SQLiteDatabase sqLiteDatabase = mysqlite.getReadableDatabase();
        String sql = "select * from rfid where number=?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{number});
        //Cursor cursor = sqLiteDatabase.query("rfid", null, null, null, null, null, null);
      
        if (cursor != null) {
            while (cursor.moveToNext()) {
            	EntityTag item = new EntityTag();
                item.setNumber(cursor.getString(cursor.getColumnIndex("number")));
                item.setEpcData(cursor.getString(cursor.getColumnIndex("epcData")));
                items.add(item);
                Log.i("tiger", "" + item.toString());
            }
        }
        return items;
    }

    //通过epc查询单个标签
    public boolean queryEPCbyid(String epcData) {
        SQLiteDatabase sqLiteDatabase = mysqlite.getReadableDatabase();
        String sql = "select * from rfid where epcData=?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{epcData});
        // Cursor cursor = sqLiteDatabase.query("good", null, null, null, null, null, null);
       if(cursor.moveToNext()){
    	   //如果有相同的标签内容
    	   return false;
       }
       //没有相同标签就添加
       return true;
    }

    //淇敼璐墿杞︾殑鏁伴噺
    public void updatecount(int count, int id) {
        SQLiteDatabase sqLiteDatabase = mysqlite.getWritableDatabase();
        String sql = "update rfid set count=? where id=?";
        sqLiteDatabase.execSQL(sql, new Object[]{count, id});
        // ContentValues cv = new ContentValues();
        // cv.put("username", user.getUsername());
        // cv.put("password", user.getPassword());
        // sqLiteDatabase.update("user", cv, "id=?", new String[]{"" + user.getId()});
    
    }
    
}
