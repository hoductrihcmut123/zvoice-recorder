package com.example.myapplication1.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
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
import com.example.myapplication1.utils.OnItemClickListener
import com.example.myapplication1.R
import com.example.myapplication1.database.SQLiteHelper
import com.example.myapplication1.database.model.AudioRecordModel
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

    private lateinit var records: ArrayList<AudioRecordModel>
    private lateinit var recordsTemp: ArrayList<AudioRecordModel>
    private lateinit var myAdapter: Adapter
    private lateinit var myAdapterSecond: MultiSelectAdapter
    private lateinit var db: SQLiteHelper
    private var ar: AudioRecordModel? = null
    private lateinit var sqLiteHelper: SQLiteHelper

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetDeleteBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetRenameBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetMultiDeleteBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient

    private var checklistDisable = true
    private var isMultipleUpload = false

    private var filenameList = ArrayList<String>()
    private var filePathList = ArrayList<String>()


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

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
            .requestScopes(
                Scope(DriveScopes.DRIVE_FILE)
            ).build()
        gsc = GoogleSignIn.getClient(this, gso)

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredRecords = ArrayList<AudioRecordModel>()
                if (binding.searchInput.text!!.isNotEmpty()) {
                    for (i in 0 until records.size) {
                        if (records[i].filename.lowercase().contains(s.toString().lowercase())) {
                            filteredRecords.add(records[i])
                        }
                    }
                    myAdapter = Adapter(filteredRecords, this@GalleryActivity)
                    binding.recyclerview.adapter = myAdapter
                    recordsTemp = filteredRecords
                } else {
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
        binding.galleryToolbar.setNavigationOnClickListener {
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

        val bottomSheetMultiDelete = findViewById<LinearLayout>(R.id.bottomSheetMultiDelete)
        bottomSheetMultiDeleteBehavior = BottomSheetBehavior.from(bottomSheetMultiDelete)
        bottomSheetMultiDeleteBehavior.peekHeight = 0
        bottomSheetMultiDeleteBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetMultiDeleteBehavior.isDraggable = false

        binding.bottomSheetGalleryBG.setOnClickListener {
            collapseFirst()
        }

        binding.bottomSheetDeleteBG.setOnClickListener {}

        binding.bottomSheetRenameBG.setOnClickListener {}

        binding.bottomSheetGallery.btnDeleteGallery.setOnClickListener {
            collapseFirst()
            bottomSheetDeleteBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetDeleteBG.visibility = View.VISIBLE
        }

        binding.bottomSheetDelete.btnCancelDelete.setOnClickListener {
            collapseSecond()
        }

        binding.bottomSheetDelete.btnOkDelete.setOnClickListener {

            GlobalScope.launch(Dispatchers.IO) {
                ar?.let { it1 -> deleteRecord(it1.id, ar!!) }
                withContext(Dispatchers.Main) {
                    fetchAll()
                    collapseSecond()
                    binding.searchInput.text?.clear()
                    Toast.makeText(this@GalleryActivity, "Deleted successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.bottomSheetGallery.btnRename.setOnClickListener {
            collapseFirst()
            bottomSheetRenameBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetRenameBG.visibility = View.VISIBLE

            binding.bottomSheetRename.filenameInputRename.setText(binding.bottomSheetGallery.filenameGallery.text)
            binding.bottomSheetRename.filenameInputRename.requestFocus()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        binding.bottomSheetRename.btnCancelRename.setOnClickListener {
            collapseThird()
        }

        binding.bottomSheetRename.btnOkRename.setOnClickListener {
            renameRecord()
        }

        binding.bottomSheetGallery.btnUpload.setOnClickListener {
            isMultipleUpload = false
            signIn()
        }

        binding.checklist.setOnClickListener {
            checklistDisable = false
            binding.checklist.visibility = View.GONE
            binding.tvCancel.visibility = View.VISIBLE
            binding.textInputLayout.visibility = View.GONE
            binding.constraintLayout.visibility = View.VISIBLE
            hideKeyboard(binding.searchInput)
            binding.searchInput.text?.clear()
            if (Build.VERSION.SDK_INT >= 28) {
                binding.appBarLayout.outlineAmbientShadowColor =
                    resources.getColor(R.color.strokeColor)
                binding.appBarLayout.outlineSpotShadowColor =
                    resources.getColor(R.color.strokeColor)
            }

            myAdapterSecond = MultiSelectAdapter(records, this)
            binding.recyclerview.adapter = myAdapterSecond
        }

        binding.tvCancel.setOnClickListener {
            cancelChecklist()
        }

        binding.btnSelectAll.setOnClickListener {
            if (MultiSelectAdapter.itemSelectedList.size < records.size) {
                MultiSelectAdapter.itemSelectedList.clear()
                MultiSelectAdapter.itemSelectedList.addAll(Array(records.size) { it })
            } else {
                MultiSelectAdapter.itemSelectedList.clear()
            }

            val size = MultiSelectAdapter.itemSelectedList.size
            binding.selected.text = "$size Selected"

            myAdapterSecond = MultiSelectAdapter(records, this)
            binding.recyclerview.adapter = myAdapterSecond
        }

        binding.btnMultipleDelete.setOnClickListener {
            if(MultiSelectAdapter.itemSelectedList.size > 0){
                bottomSheetMultiDeleteBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.bottomSheetDeleteBG.visibility = View.VISIBLE
                binding.bottomSheetMultiDelete.bottomSheetMultiDeleteTitle.text =
                    "Are you sure you want to delete ${MultiSelectAdapter.itemSelectedList.size} items?"
            }
        }

        binding.bottomSheetMultiDelete.btnOkMultiDelete.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                for (i in 0 until MultiSelectAdapter.itemSelectedList.size) {
                    val position = MultiSelectAdapter.itemSelectedList[i]
                    val id = records[position].id
                    deleteRecord(id, records[position])
                }
                withContext(Dispatchers.Main) {
                    collapseMultiDelete()
                    Toast.makeText(
                        this@GalleryActivity,
                        "Deleted ${MultiSelectAdapter.itemSelectedList.size} items successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    cancelChecklist()
                    fetchAll()
                }
            }
        }

        binding.bottomSheetMultiDelete.btnCancelMultiDelete.setOnClickListener {
            collapseMultiDelete()
        }

        binding.btnMultipleUpload.setOnClickListener {
            if(MultiSelectAdapter.itemSelectedList.size > 0){
                GlobalScope.launch(Dispatchers.IO) {
                    for (i in 0 until MultiSelectAdapter.itemSelectedList.size) {
                        val position = MultiSelectAdapter.itemSelectedList[i]
                        filenameList += records[position].filename
                        filePathList += records[position].filePath
                    }
                }
                isMultipleUpload = true
                signIn()
            }
        }

    }

    private fun fetchAll() {
        GlobalScope.launch(Dispatchers.IO) {
            records.clear()
            val queryResult = db.getAllAudioRecord()

//            for(i in (0..100)){
//                db.deleteAudioRecordById(i)
//            }
            records.addAll(queryResult)
            recordsTemp = records

            withContext(Dispatchers.Main) {
                myAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClickListener(position: Int) {
        //Toast.makeText(this, "$position", Toast.LENGTH_SHORT).show()
        val audioRecord = recordsTemp[position]
        if (checklistDisable) {
            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("filePath", audioRecord.filePath)
            intent.putExtra("filename", audioRecord.filename)
            startActivity(intent)
        } else {
            val size = MultiSelectAdapter.itemSelectedList.size
            binding.selected.text = "$size Selected"
        }
    }

    override fun onItemLongClickListener(position: Int) {
        //Toast.makeText(this, "Long Click", Toast.LENGTH_SHORT).show()
        hideKeyboard(binding.searchInput)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetGalleryBG.visibility = View.VISIBLE
            binding.bottomSheetGallery.filenameGallery.text = recordsTemp[position].filename
        }, 250)
        ar = recordsTemp[position]
    }

    private fun collapseFirst() {
        binding.bottomSheetGalleryBG.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 50)
    }

    private fun collapseSecond() {
        binding.bottomSheetDeleteBG.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetDeleteBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 50)
    }

    private fun collapseThird() {
        binding.bottomSheetRenameBG.visibility = View.GONE
        hideKeyboard(binding.bottomSheetRename.filenameInputRename)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetRenameBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 200)
    }

    private fun collapseMultiDelete() {
        binding.bottomSheetDeleteBG.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetMultiDeleteBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 50)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        MultiSelectAdapter.itemSelectedList.clear()
        hideKeyboard(binding.searchInput)
    }

    private fun renameRecord() {
        val filename = binding.bottomSheetRename.filenameInputRename.text.toString()

        if (ar == null) return

        if (filename == ar!!.filename) {
            Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val status = sqLiteHelper.updateAudioRecordName(
                AudioRecordModel(
                    id = ar!!.id,
                    filename = filename
                )
            )
            withContext(Dispatchers.Main) {
                collapseThird()
                binding.searchInput.text?.clear()
                if (status > -1) {
                    fetchAll()
                    Toast.makeText(this@GalleryActivity, "Renamed successfully", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this@GalleryActivity, "Renamed failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun deleteRecord(id: Int, ar: AudioRecordModel) {
        sqLiteHelper.deleteAudioRecordById(id)
        File(ar.filePath).delete()
    }

    private fun signIn() {
        val signInIntent = gsc.signInIntent
        startActivityForResult(signInIntent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                task.getResult(ApiException::class.java)
                if(isMultipleUpload){
                    MultiSelectAdapter.itemSelectedList.clear()
                    finish()
                    navigateToMultiUploadActivity()
                } else{
                    finish()
                    navigateToUploadActivity()
                }
            } catch (e: ApiException) {
                filenameList.clear()
                filePathList.clear()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToUploadActivity() {
        val intent = Intent(this, UploadActivity::class.java)
        intent.putExtra("filePath", ar?.filePath)
        intent.putExtra("filename", ar?.filename)
        startActivity(intent)
    }

    private fun navigateToMultiUploadActivity() {
        val intent = Intent(this, MultiUploadActivity::class.java)
        intent.putExtra("filenameList", filenameList)
        intent.putExtra("filePathList", filePathList)
        startActivity(intent)
    }

    private fun cancelChecklist() {
        checklistDisable = true
        binding.checklist.visibility = View.VISIBLE
        binding.tvCancel.visibility = View.GONE
        binding.textInputLayout.visibility = View.VISIBLE
        binding.constraintLayout.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= 28) {
            binding.appBarLayout.outlineAmbientShadowColor = Color.TRANSPARENT
            binding.appBarLayout.outlineSpotShadowColor = Color.TRANSPARENT
        }
        binding.selected.text = "0 Selected"
        MultiSelectAdapter.itemSelectedList.clear()
        myAdapter = Adapter(records, this)
        binding.recyclerview.adapter = myAdapter
    }

}