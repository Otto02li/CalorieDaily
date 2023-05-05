package com.example.caloriedaily

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.DatePicker
import android.widget.EditText
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_calorie_data.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.datepickerdialog_edittext.*
import java.util.*
import kotlin.math.log


class CalorieData : AppCompatActivity(),View.OnClickListener{

//    private var datelist:MutableList<String> = mutableListOf()  //存放日期
//    private var colorielist:MutableList<Int> = mutableListOf()  //存放卡路里数值
    private var list:MutableList<Record> = mutableListOf()        //存放记录队列
    private var datalist:MutableList<MutableMap<String,Any>> = mutableListOf()  //数据队列
    private val from:Array<String> = arrayOf("date","calorie")    //key值数组
    private val to:IntArray= intArrayOf(R.id.record_date,R.id.record_calorie)   //列表项控件id数组
//    private val db = openwritedb()
//    private var adapter:SimpleAdapter = SimpleAdapter(this,datalist,R.layout.record_item,from,to)
    private var value_date=""
    private var edittext: EditText? =null       //编辑文本框
    private var mydatepicker: DatePicker? = null        //日期选择对话框
    //DatePickerDialog的监听回调函数
    private var listener: OnDateSetListener? =
        OnDateSetListener { datePicker, year, monthOfYear, dayOfMonth ->
            fun checkTime(i:Int):String{    //判断输入的月份日数是否为个位数，转换为mmordd的标准输出
                if (i<10){
                    return "0$i"
                }
                return i.toString()
            }

//            Log.d("insert","monthOfYear类型：${monthOfYear::class.simpleName}")  //Int
//            while (true){
//                value_date="$year-${checkTime(monthOfYear+1)}-${checkTime(dayOfMonth)}"
//                if (value_date != "")
//                    break
//            }
//            val year1 = mydatepicker.month
            while (true){
                if (mydatepicker!=null)
                    break
            }
            value_date="${mydatepicker!!.year}-${checkTime(mydatepicker!!.month+1)}-${checkTime(mydatepicker!!.dayOfMonth)}"
//            value_date="$year-${checkTime(monthOfYear+1)}-${checkTime(dayOfMonth)}"
//            Log.d("insert","date:$value_date")      //date没更改，导致sqlite插入错误
            //SQLite执行insert操作
            val db = openwritedb()
            //
//            val edittext:EditText = findViewById(R.id.input)    //debug
//            Log.d("insert","edit")
            while (true){
                if (edittext!=null)
                    break
            }
            var calorie = edittext?.text.toString().toInt()
            while (true) {
                calorie = edittext?.text.toString().toInt()
                if (calorie >= 0)
                    break
            }

//            Log.d("insert","calorie:$calorie")
            insertrecord(db,value_date,calorie)
//            insertrecord(db,value_date,0)
            value_date=""
            calorie = -1000
            list.clear()
            datalist.clear()
            getrecord(db)
            initDataList()
//            Log.d("insert","insert后list:$list")
//            Log.d("insert","datalist:$datalist")
            val adapter:SimpleAdapter = SimpleAdapter(this,datalist,R.layout.record_item,from,to)
            showrecord(adapter)
            db.close()
        }



    //设置按钮点击事件
    fun setClickEvent(){
        btn_add.setOnClickListener(this)
    }

    //实现接口的onClick方法
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_add->add_record()
        }
    }

    //点击btn_add的事件,添加记录
    fun add_record(){
        val layoutInflater = LayoutInflater.from(this)
        val dateeditDialog = layoutInflater.inflate(R.layout.datepickerdialog_edittext,findViewById(R.id.item_lin_ed))

        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this,listener, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        dialog.setView(dateeditDialog)  //加载自定义布局

        //编辑文本框
        dialog.show()   //需先显示，然后才能查找控件
        edittext=dialog.findViewById(R.id.input)
        mydatepicker =dialog.findViewById(R.id.date_picker_actions)
    }

    //打开写数据库
    fun openwritedb():SQLiteDatabase{
        val dbHelper=CalorieDatabaseHelper(this,"CalorieRecord",1)
        val db:SQLiteDatabase = dbHelper.writableDatabase
        return db
    }

    //打开读数据库
    fun openreaddb():SQLiteDatabase{
        val dbHelper=CalorieDatabaseHelper(this,"CalorieRecord",1)
        val db:SQLiteDatabase = dbHelper.readableDatabase
        return db
    }

    //关闭数据库
    fun closedb(db:SQLiteDatabase){
        db.close()
    }

    //获取记录
    fun getrecord(db:SQLiteDatabase,tablename:String="calorie_record"){
        //获得Cursor对象
        var c: Cursor = db.query(tablename,null,null,null,null,null,null)

        if(c.moveToFirst()){
            do{
                val date=c.getString(c.getColumnIndexOrThrow("date"))
                val calorie=c.getInt(c.getColumnIndexOrThrow("calorie"))
                val record:Record = Record(date,calorie)
//                datelist.add(date)
//                colorielist.add(calorie)
                list.add(record)
//                Log.d("date","${record.date}")
//                Log.d("date","${record.calorie}")
//                Log.d("calorie","$calorie")
            }while (c.moveToNext())
        }
            c.close()
    }

    //插入记录
    fun insertrecord(db:SQLiteDatabase,date:String,calorie:Int){
        //插入数据
        val data1 = ContentValues().apply {
            put("date",date)
            put("calorie",calorie)
        }
        db.insert("calorie_record",null,data1)
    }

    //删除记录
    fun deleterecord(db:SQLiteDatabase,adapter: SimpleAdapter,index:Int){
        val deldata=list.get(index)  //要删除的记录

        var c: Cursor = db.query("calorie_record",null,null,null,null,null,null)
//        Log.d("del","c:$c")
            while (c.moveToNext()){
                val date=c.getString(c.getColumnIndexOrThrow("date"))
                val calorie=c.getInt(c.getColumnIndexOrThrow("calorie"))

                //判断哪一条记录与要删除的匹配
                if(deldata.date.equals(date) && deldata.calorie.equals(calorie)){
//                    Log.d("del","find equal data")
                    deletedata(db,date,adapter)
                    break
                }
            }
        c.close()
    }


    //从数据库中删除符合的记录
    fun deletedata(db:SQLiteDatabase,deldate:String,adapter: SimpleAdapter){
//        db.execSQL("delete from calorie_record where date LIKE $deldate") //注意空格，否则执行失败
        val selection = "date LIKE ?"
        val selectionArgs = arrayOf(deldate)
        val deletedRows = db.delete("calorie_record",selection,selectionArgs)
//        Log.d("del","delete执行后从数据库中删除的行数:$deletedRows")
        // 清除原来的数据并更新adapter
        list.clear()
        datalist.clear()
        adapter.notifyDataSetChanged()
        list_view.adapter=adapter
    }

    //显示数据
    fun showrecord(adapter: SimpleAdapter){
        /**
         * 传参给CalorieDatabaseHelper类，CalorieDatabaseHelper类再把参数传给父类SQLiteOpenHelper创建数据库
         * context: 操作数据库
         * name:要创建的数据库名称
         * version:数据库版本号
         */
        adapter.notifyDataSetChanged()
        list_view.adapter=adapter
    }

    //初始化适配器需要的数据格式
    private fun initDataList(){
        for(i in list.indices){
            var map:MutableMap<String,Any> = mutableMapOf()
            map.put("date",list.get(i).date)
            map.put("calorie",list.get(i).calorie)
            datalist.add(map)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calorie_data)

//        adapter = SimpleAdapter(this,datalist,R.layout.record_item,from,to)  //listview适配器
        val adapter:SimpleAdapter = SimpleAdapter(this,datalist,R.layout.record_item,from,to)  //listview适配器
        var db = openwritedb()
//        insertrecord(db,"2023-04-28",2800)
        getrecord(db)
        initDataList()
        showrecord(adapter)

        //debug
//        db.close()

        setClickEvent()

//        deleterecord(db)
//        getrecord(db)
//        showrecord()

        //list_view长按某一item事件
        list_view.onItemLongClickListener = OnItemLongClickListener({ adapterView, view, i, l->
            //代码块
            db=openwritedb()
            val builder:AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Item Dialog")
                .setMessage("Are you sure to delete this record?")
                .setCancelable(true)   //可否用back键关闭对话框属性
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    //删除记录操作
                    deleterecord(db,adapter,i)
    //                    Log.d("del","getrecord(db)前，list:$list")
    //                    Log.d("del","getrecord(db)前，datalist:$datalist")
                    getrecord(db)
                    initDataList()
    //                    Log.d("del","getrecord(db)后，list:$list")
    //                    Log.d("del","getrecord(db)后，datalist:$datalist")
                    showrecord(adapter)
                    db.close()
    //                    Log.d("del","delete success")
                })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, id ->

                })
            builder.create().show()

            true})

//        closedb(db)   //debug:already-closed
    }
}