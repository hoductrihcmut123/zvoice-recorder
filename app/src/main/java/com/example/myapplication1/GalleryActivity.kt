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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityGalleryBinding

    private lateinit var records : ArrayList<AudioRecordModel>
    private lateinit var recordsTemp : ArrayList<AudioRecordModel>
    private lateinit var myAdapter : Adapter
    private lateinit var db : SQLiteHelper
    private var ar: AudioRecordModel? = null
    private lateinit var sqLiteHelper: SQLiteHelper

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetDeleteBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetRenameBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var gso : GoogleSignInOptions
    private lateinit var gsc : GoogleSignInClient


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

        sqLiteHelper = SQLiteHelper(this)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(
            Scope(DriveScopes.DRIVE_FILE)
        ).build()
        gsc = GoogleSignIn.getClient(this, gso)

        binding.searchInput.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredRecords = ArrayList<AudioRecordModel>()
                if (binding.searchInput.text!!.isNotEmpty())
                {
                    for(i in 0 until records.size)
                    {
                        if(records[i].filename.lowercase().contains(s.toString().lowercase())) {
                            filteredRecords.add(records[i])
                        }
                    }
                    myAdapter = Adapter(filteredRecords, this@GalleryActivity)
                    binding.recyclerview.adapter = myAdapter
                    recordsTemp = filteredRecords
                }
                else{
                    myAdapter = Adapter(records, this@GalleryActivity)
                    binding.recyclerview.adapter = myAdapter
                    recordsTemp = records
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

        val bottomSheetGallery = findViewById<LinearLayout>(R.id.bottomSheetGallery)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetGallery)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isDraggable = false

        val bottomSheetDelete = findViewById<LinearLayout>(R.id.bottomSheetDelete)
        bottomSheetDeleteBehavior = BottomSheetBehavior.from(bottomSheetDelete)
        bottomSheetDeleteBehavior.peekHeight = 0
        bottomSheetDeleteBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetDeleteBehavior.isDraggable = false

        val bottomSheetRename = findViewById<LinearLayout>(R.id.bottomSheetRename)
        bottomSheetRenameBehavior = BottomSheetBehavior.from(bottomSheetRename)
        bottomSheetRenameBehavior.peekHeight = 0
        bottomSheetRenameBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetRenameBehavior.isDraggable = false

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

            GlobalScope.launch(Dispatchers.IO) {
                ar?.let { it1 -> deleteRecord(it1.id) }
                withContext(Dispatchers.Main) {
                    fetchAll()
                    collapseSecond()
                    binding.searchInput.text?.clear()
                    Toast.makeText(this@GalleryActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
                }
            }
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
            renameRecord()
        }

        binding.bottomSheetGallery.btnUpload.setOnClickListener{
            signIn()
        }

    }

    private fun fetchAll(){
        GlobalScope.launch (Dispatchers.IO){
            records.clear()
            val queryResult = db.getAllAudioRecord()

//            for(i in (0..100)){
//                db.deleteAudioRecordById(i)
//            }
            records.addAll(queryResult)
            recordsTemp = records

            withContext(Dispatchers.Main){
                myAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClickListener(position: Int) {
        //Toast.makeText(this, "$position", Toast.LENGTH_SHORT).show()
        val audioRecord = recordsTemp[position]
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("filePath", audioRecord.filePath)
        intent.putExtra("filename", audioRecord.filename)
        startActivity(intent)
    }

    override fun onItemLongClickListener(position: Int) {
        //Toast.makeText(this, "Long Click", Toast.LENGTH_SHORT).show()
        hideKeyboard(binding.searchInput)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetGalleryBG.visibility = View.VISIBLE
            binding.bottomSheetGallery.filenameGallery.text = recordsTemp[position].filename
        },250)
        ar = recordsTemp[position]
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
        },200)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard(binding.searchInput)
    }

    private fun renameRecord(){

        val filename = binding.bottomSheetRename.filenameInputRename.text.toString()

        if(ar == null) return

        if(filename == ar!!.filename){
            Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val  status = sqLiteHelper.updateAudioRecord(AudioRecordModel(id =  ar!!.id, filename = filename))
            withContext(Dispatchers.Main) {
                collapseThird()
                binding.searchInput.text?.clear()
                if (status > -1) {
                    fetchAll()
                    Toast.makeText(this@GalleryActivity, "Renamed successfully", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@GalleryActivity, "Renamed failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteRecord(id: Int) {
        sqLiteHelper.deleteAudioRecordById(id)
        File("${ar?.filePath}").delete()
        File("${ar?.ampsPath}").delete()
    }

    private fun signIn(){
        val signInIntent = gsc.signInIntent
        startActivityForResult(signInIntent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1000){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                task.getResult(ApiException::class.java)
                finish()
                navigateToUploadActivity()
            } catch (e : ApiException) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToUploadActivity(){
        val intent = Intent(this, UploadActivity::class.java)
        intent.putExtra("filePath", ar?.filePath)
        intent.putExtra("filename", ar?.filename)
        startActivity(intent)
    }

}