package com.example.caloriedaily

import android.app.DatePickerDialog
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity(), View.OnClickListener {
//    private var list:MutableList<Record> = mutableListOf()        //存放记录队列
    private var datapointarray:Array<DataPoint?> = arrayOf()               //DataPoint数组
    private var series:LineGraphSeries<DataPoint>? = null
    private val format = SimpleDateFormat("yyyy-MM-dd")            //时间格式
    private var begindate:Date=Date()
    private var enddate:Date=Date()

    //设置按钮点击事件
    fun setClickEvent(){
        btn_edit.setOnClickListener(this)
        btn_begin.setOnClickListener(this)
        btn_end.setOnClickListener(this)
        btn_query.setOnClickListener(this)
    }

    //实现接口的onClick方法
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_edit->switchToCalorieDataAct()
            R.id.btn_begin->getbegndate()
            R.id.btn_end->getenddate()
            R.id.btn_query->query()
        }
    }

    //btn_edit点击事件
    fun switchToCalorieDataAct(){
        val intent=Intent(this@MainActivity,CalorieData::class.java)
        startActivity(intent)
    }

    //btn_begin点击事件
    fun getbegndate(){
        //弹出datepickerDialog
        val dialog = DatePickerDialog(this)
        dialog.show()
        dialog.setOnDateSetListener { datePicker, year, month, day ->
            fun checkTime(i:Int):String{    //判断输入的月份日数是否为个位数，转换为mmordd的标准输出
                if (i<10){
                    return "0$i"
                }
                return i.toString()
            }

            tv_begin.text = "Begin"+"\t\t\t\t\t$year-${checkTime(month+1)}-${checkTime(day)}"
            begindate = format.parse("$year-${checkTime(month+1)}-${checkTime(day)}")
//            Log.d("query","begindate:$year-${checkTime(month+1)}-${checkTime(day)}")
        }
    }

    //btn_end点击事件
    fun getenddate(){
        //弹出datepickerDialog
        val dialog = DatePickerDialog(this)
        dialog.show()
        dialog.setOnDateSetListener { datePicker, year, month, day ->
            fun checkTime(i:Int):String{    //判断输入的月份日数是否为个位数，转换为mmordd的标准输出
                if (i<10){
                    return "0$i"
                }
                return i.toString()
            }

            tv_end.text = "End"+"\t\t\t\t\t\t\t\t\t$year-${checkTime(month+1)}-${checkTime(day)}"
            enddate = format.parse("$year-${checkTime(month+1)}-${checkTime(day)}")
//            Log.d("query","enddate:$year-${checkTime(month+1)}-${checkTime(day)}")
        }
    }

    //btn_query事件
    fun query(){
        //清空datapointarray，series里的所有元素，为了再次点击btn_query可以正常使用
//        datapointarray.drop(datapointarray.size)    //没起效果
//        datapointarray.fill(DataPoint(Date,Double),0,datapointarray.size)
        datapointarray= arrayOf()
        series?.resetData(datapointarray)

//        series
        //从SQLite数据库获得按升序排列的数据
        val dbHelper=CalorieDatabaseHelper(this,"CalorieRecord",1)
        val db: SQLiteDatabase = dbHelper.readableDatabase
        //查询select * from calorie_record order by date(date) asc
        //获得Cursor对象
        val c: Cursor = db.query("calorie_record",null,null,null,null,null,"date(date) asc")
        if(c.moveToFirst()){
            do{
                val date=c.getString(c.getColumnIndexOrThrow("date"))
                val calorie=c.getInt(c.getColumnIndexOrThrow("calorie"))
//                val dataPoint:DataPoint = DataPoint(date.toDouble(),calorie.toDouble())       //bug
                val datax = format.parse(date) //转换为格林威治时间（unix时间戳）
                val dataPoint:DataPoint = DataPoint(datax,calorie.toDouble())
                //判断record日期是否在范围内
                if (datax>=begindate&&datax<=enddate){
                    addItem(dataPoint)  //将数据加入到datapointarray数组末尾
                    Log.d("query","$date")
                }

//                Log.d("query","success${datapointarray.size}")
//                Log.d("query","$date$calorie")
            }while (c.moveToNext())
        }
        c.close()
        db.close()
        //将数据更新到GraphView,创建折线图
//        Log.d("query","success")
        drawGraphView()
//        Log.d("query","success")
    }

    //datapointarray,Array数组添加元素的方法需要自己写
    fun addItem(item:DataPoint){
        var newArr: Array<DataPoint?> = arrayOfNulls(datapointarray.size+1)
        for (i in datapointarray.indices){
            newArr[i]=datapointarray[i]
        }

        newArr[datapointarray.size] = item
        datapointarray = newArr
    }

    //更新折线图
    fun drawGraphView(){
//        graph.removeAllSeries() //清空
        if(!graph.series.isEmpty()){
            graph.removeAllSeries()
            Log.d("query","removeAllSeries()")
            Log.d("query","${datapointarray.size}")
            Log.d("query","${series?.isEmpty}")
        }
        series = LineGraphSeries(datapointarray)
        Log.d("query","${datapointarray.size}")
        graph.addSeries(series)

//        Log.d("query","success")
        graph.gridLabelRenderer.labelFormatter= DateAsXAxisLabelFormatter(this)
        graph.viewport.isScalable=true      //可缩放
//        Log.d("query","success")
//        graph.gridLabelRenderer.numHorizontalLabels=datapointarray.size+2       //debugdatapointarray.size=0
////        Log.d("query","success${datapointarray.size}")
//        Log.d("query","${datapointarray.first()}")
//        graph.viewport.setMinX(datapointarray.first()!!.x)
//        graph.viewport.setMinX(datapointarray.last()!!.x)
//        graph.viewport.isXAxisBoundsManual=true
//
//        graph.gridLabelRenderer.setHumanRounding(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setClickEvent()
    }
}