package com.example.myapplication1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records : ArrayList<AudioRecordModel>
    private lateinit var myAdapter : Adapter
    private lateinit var db : SQLiteHelper
    private lateinit var recyclerview: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        records = ArrayList()
        db = SQLiteHelper(this)
        myAdapter = Adapter(records, this)
        initView()
        recyclerview.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()
    }

    private fun initView(){
        recyclerview = findViewById(R.id.recyclerview)
    }

    private fun fetchAll(){
        GlobalScope.launch {
            records.clear()
            var queryResult = db.getAllAudioRecord()

//            for(i in (0..100)){
//                db.deleteAudioRecordById(i)
//            }
            records.addAll(queryResult)

            myAdapter.notifyDataSetChanged()
        }
    }

    override fun onItemClickListener(position: Int) {
        Toast.makeText(this, "Short Click", Toast.LENGTH_SHORT).show()
    }

    override fun onItemLongClickListener(position: Int) {
        Toast.makeText(this, "Long Click", Toast.LENGTH_SHORT).show()
    }
}