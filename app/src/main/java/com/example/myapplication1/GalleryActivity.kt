package com.example.myapplication1

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records : ArrayList<AudioRecordModel>
    private lateinit var myAdapter : Adapter
    private lateinit var db : SQLiteHelper
    private lateinit var recyclerview: RecyclerView

    private lateinit var searchInput: TextInputEditText

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

        searchInput = findViewById(R.id.search_input)
        searchInput.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var filteredRecords = ArrayList<AudioRecordModel>()
                if (searchInput.text!!.isNotEmpty())
                {
                    for(i in 0 until records.size)
                    {
                        if(records[i].filename!!.lowercase().contains(s.toString().lowercase()))
                            filteredRecords.add(records[i])
                    }
                    myAdapter = Adapter(filteredRecords, this@GalleryActivity)
                    recyclerview.adapter = myAdapter
                }
                else{
                    myAdapter = Adapter(records, this@GalleryActivity)
                    recyclerview.adapter = myAdapter
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        
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
        //Toast.makeText(this, "$position", Toast.LENGTH_SHORT).show()
        var audioRecord = records[position]
        var intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("filePath", audioRecord.filePath)
        intent.putExtra("filename", audioRecord.filename)
        startActivity(intent)
    }

    override fun onItemLongClickListener(position: Int) {
        //Toast.makeText(this, "Long Click", Toast.LENGTH_SHORT).show()
    }
}