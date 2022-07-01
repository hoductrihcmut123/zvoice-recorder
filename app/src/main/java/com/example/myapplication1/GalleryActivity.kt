package com.example.myapplication1

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication1.databinding.ActivityGalleryBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityGalleryBinding

    private lateinit var records : ArrayList<AudioRecordModel>
    private lateinit var myAdapter : Adapter
    private lateinit var db : SQLiteHelper

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetDeleteBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetRenameBehavior: BottomSheetBehavior<LinearLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        records = ArrayList()
        db = SQLiteHelper(this)
        myAdapter = Adapter(records, this)

        binding.recyclerview.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()

        binding.searchInput.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var filteredRecords = ArrayList<AudioRecordModel>()
                if (binding.searchInput.text!!.isNotEmpty())
                {
                    for(i in 0 until records.size)
                    {
                        if(records[i].filename.lowercase().contains(s.toString().lowercase()))
                            filteredRecords.add(records[i])
                    }
                    myAdapter = Adapter(filteredRecords, this@GalleryActivity)
                    binding.recyclerview.adapter = myAdapter
                }
                else{
                    myAdapter = Adapter(records, this@GalleryActivity)
                    binding.recyclerview.adapter = myAdapter
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setSupportActionBar(binding.galleryToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.galleryToolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        var bottomSheetGallery = findViewById<LinearLayout>(R.id.bottomSheetGallery)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetGallery)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        var bottomSheetDelete = findViewById<LinearLayout>(R.id.bottomSheetDelete)
        bottomSheetDeleteBehavior = BottomSheetBehavior.from(bottomSheetDelete)
        bottomSheetDeleteBehavior.peekHeight = 0
        bottomSheetDeleteBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        var bottomSheetRename = findViewById<LinearLayout>(R.id.bottomSheetRename)
        bottomSheetRenameBehavior = BottomSheetBehavior.from(bottomSheetRename)
        bottomSheetRenameBehavior.peekHeight = 0
        bottomSheetRenameBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.bottomSheetGalleryBG.setOnClickListener{
            collapseFirst()
        }

        binding.bottomSheetDeleteBG.setOnClickListener{}

        binding.bottomSheetRenameBG.setOnClickListener{}

        binding.bottomSheetGallery.btnDeleteGallery.setOnClickListener{
            collapseFirst()
            bottomSheetDeleteBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetDeleteBG.visibility = View.VISIBLE
        }

        binding.bottomSheetDelete.btnCancelDelete.setOnClickListener{
            collapseSecond()
        }

        binding.bottomSheetDelete.btnOkDelete.setOnClickListener{
            collapseSecond()
            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
        }

        binding.bottomSheetGallery.btnRename.setOnClickListener{
            collapseFirst()
            bottomSheetRenameBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetRenameBG.visibility = View.VISIBLE

            binding.bottomSheetRename.filenameInputRename.setText(binding.bottomSheetGallery.filenameGallery.text)
            binding.bottomSheetRename.filenameInputRename.requestFocus()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        binding.bottomSheetRename.btnCancelRename.setOnClickListener{
            collapseThird()
        }

        binding.bottomSheetRename.btnOkRename.setOnClickListener{
            collapseThird()
            Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show()
        }


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
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.bottomSheetGalleryBG.visibility = View.VISIBLE
        binding.bottomSheetGallery.filenameGallery.text = records[position].filename
    }

    private fun collapseFirst(){
        binding.bottomSheetGalleryBG.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        },50)
    }

    private fun collapseSecond(){
        binding.bottomSheetDeleteBG.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetDeleteBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        },50)
    }

    private fun collapseThird(){
        binding.bottomSheetRenameBG.visibility = View.GONE
        hideKeyboard(binding.bottomSheetRename.filenameInputRename)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetRenameBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        },50)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}