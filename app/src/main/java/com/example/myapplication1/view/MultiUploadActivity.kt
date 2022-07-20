package com.example.myapplication1.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication1.R
import com.example.myapplication1.databinding.ActivityMultiUploadBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MultiUploadActivity : AppCompatActivity() {

    private lateinit var gso : GoogleSignInOptions
    private lateinit var gsc : GoogleSignInClient

    private lateinit var binding: ActivityMultiUploadBinding

    private var filenameList = ArrayList<String>()
    private var filePathList = ArrayList<String>()
    private var testPairList = ArrayList<Pair<String,String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filenameList = intent.getStringArrayListExtra("filenameList") as ArrayList<String>
        filePathList = intent.getStringArrayListExtra("filePathList") as ArrayList<String>

        for (i in 0 until filenameList.size){
            testPairList += Pair(filenameList[i],filePathList[i])
        }

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        gsc = GoogleSignIn.getClient(this, gso)

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if(acct != null){
            val personEmail = acct.email
            binding.accountName.text = personEmail
            binding.accountName.paintFlags = binding.accountName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        var stringFilename = ""
        val newLine = "\n"
        for (i in 0 until filenameList.size){
            stringFilename = "$stringFilename $newLine ${filenameList[i]}"
        }
        binding.tvDocumentTitles.text = stringFilename

        binding.btnCancelUpload.setOnClickListener {
            signOut()
        }

        binding.btnOkUpload.setOnClickListener{
            if (acct != null) {
                binding.btnCancelUpload.visibility = View.GONE
                binding.btnOkUpload.visibility = View.GONE
                binding.btnBack.visibility = View.VISIBLE
                binding.BG.visibility = View.VISIBLE
                binding.progressBarUpload.visibility = View.VISIBLE
                uploadFileToGDrive(acct)
            }
        }

        binding.btnBack.setOnClickListener{
            signOut()
        }

        binding.BG.setOnClickListener{}
    }

    private fun signOut(){
        gsc.signOut().addOnCompleteListener{
            finish()
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }

    private fun getDriveService(googleSignInAccount: GoogleSignInAccount): Drive? {
        val credential =
            GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))

        credential.selectedAccount = googleSignInAccount.account

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(R.string.app_name.toString()).build()
    }

    private fun uploadFileToGDrive(googleSignInAccount: GoogleSignInAccount) {
        getDriveService(googleSignInAccount)?.let {googleDriveService->
            GlobalScope.launch (Dispatchers.IO){
                var isSuccessful = true
                try {
                    var isExist = false
                    var folderId = ""
                    var pageToken: String? = null
                    do {
                        val result: FileList = googleDriveService.files().list()
                            .setQ("mimeType = 'application/vnd.google-apps.folder'")
                            .setQ("trashed = false")
                            .setSpaces("drive")
                            .setPageToken(pageToken)
                            .execute()
                        for (file in result.files) {
                            if(file.name == "ZVoice-Recorder"){
                                isExist = true
                                folderId = file.id
                            }
                        }
                        pageToken = result.nextPageToken
                    } while (pageToken != null)

                    if (!isExist){
                        val gFolder = com.google.api.services.drive.model.File()
                        gFolder.name = "ZVoice-Recorder"
                        gFolder.mimeType = "application/vnd.google-apps.folder"
                        val file = googleDriveService.Files().create(gFolder).setFields("id").execute()
                        folderId = file.id
                    }

//                    // sequential
//                    for (i in 0 until filenameList.size){
//                        val actualFile = File(filePathList[i])
//                        val gFile = com.google.api.services.drive.model.File()
//                        gFile.name = filenameList[i]
//                        gFile.parents = Collections.singletonList(folderId)
//                        val fileContent = FileContent("audio/MP3", actualFile)
//                        googleDriveService.Files().create(gFile,fileContent).execute()
//                    }

                    // parallel
                    testPairList.forEachParallel {
                        val actualFile = File(it.second)
                        val gFile = com.google.api.services.drive.model.File()
                        gFile.name = it.first
                        gFile.parents = Collections.singletonList(folderId)
                        val fileContent = FileContent("audio/MP3", actualFile)
                        googleDriveService.Files().create(gFile,fileContent).execute()
                    }
                }catch ( e: Exception){
                    e.printStackTrace()
                    isSuccessful = false
                }

                withContext(Dispatchers.Main) {
                    binding.BG.visibility = View.GONE
                    binding.progressBarUpload.visibility = View.GONE
                    if (isSuccessful){
                        Toast.makeText(this@MultiUploadActivity, "File upload successful", Toast.LENGTH_SHORT).show()
                    } else{
                        Toast.makeText(this@MultiUploadActivity, "File upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun <A>Collection<A>.forEachParallel(f: suspend (A) -> Unit): Unit = runBlocking {
        map { async(Dispatchers.Default) { f(it) } }.forEach { it.await() }
    }

}