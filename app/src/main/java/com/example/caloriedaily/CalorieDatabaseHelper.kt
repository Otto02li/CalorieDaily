package com.example.caloriedaily

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class CalorieDatabaseHelper(val context: Context,name:String,version:Int) :SQLiteOpenHelper(context,name,null,version){
    //创建卡路里记录表语句
    private val createCalorie_record = "CREATE TABLE calorie_record (" +
            "date TEXT PRIMARY KEY NOT NULL UNIQUE," +
            "calorie INTEGER NOT NULL DEFAULT 0 CHECK(calorie >= 0))"

    //创建数据库
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createCalorie_record)    //执行建表语句
//        Log.i("db","create db and table success")
    }

    //数据库版本更新
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}