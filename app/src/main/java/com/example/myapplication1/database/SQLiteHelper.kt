package com.example.myapplication1.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myapplication1.database.model.AudioRecordModel
import java.lang.Exception

class SQLiteHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "audioRecord.db"
        private const val TBL_AUDIO_RECORDS = "tbl_audioRecords"
        private const val ID = "id"
        private const val FILENAME = "filename"
        private const val FILEPATH = "filePath"
        private const val TIMESTAMP = "timestamp"
        private const val DURATION = "duration"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblAudioRecords = ("CREATE TABLE " + TBL_AUDIO_RECORDS + " ("
                + ID + " INTEGER PRIMARY KEY,"
                + FILENAME + " TEXT,"
                + FILEPATH + " TEXT,"
                + TIMESTAMP + " INTEGER,"
                + DURATION + " TEXT" + ")"
                )
        db?.execSQL(createTblAudioRecords)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_AUDIO_RECORDS")
        onCreate(db)
    }

    fun insertAudioRecord(ar: AudioRecordModel): Long{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, ar.id)
        contentValues.put(FILENAME, ar.filename)
        contentValues.put(FILEPATH, ar.filePath)
        contentValues.put(TIMESTAMP, ar.timestamp)
        contentValues.put(DURATION, ar.duration)

        val success = db.insert(TBL_AUDIO_RECORDS, null, contentValues)
        db.close()

        return success
    }

    @SuppressLint("Range")
    fun getAllAudioRecord(): ArrayList<AudioRecordModel> {
        val arList: ArrayList<AudioRecordModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_AUDIO_RECORDS"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        }catch(e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var filename: String
        var filePath: String
        var timestamp: Int
        var duration: String

        if(cursor.moveToFirst()){
            do{
                id = cursor.getInt(cursor.getColumnIndex("id"))
                filename = cursor.getString(cursor.getColumnIndex("filename"))
                filePath = cursor.getString(cursor.getColumnIndex("filePath"))
                timestamp = cursor.getInt(cursor.getColumnIndex("timestamp"))
                duration = cursor.getString(cursor.getColumnIndex("duration"))

                val ar = AudioRecordModel(id = id, filename = filename, filePath = filePath, timestamp = timestamp, duration = duration)
                arList.add(ar)
            } while (cursor.moveToNext())
        }
        return arList
    }

    fun updateAudioRecordName(ar: AudioRecordModel): Int{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, ar.id)
        contentValues.put(FILENAME, ar.filename)

        val success = db.update(TBL_AUDIO_RECORDS, contentValues, "id=" + ar.id, null)
        db.close()
        return success
    }

    fun updateAudioRecordNameDuration(ar: AudioRecordModel): Int{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, ar.id)
        contentValues.put(FILENAME, ar.filename)
        contentValues.put(DURATION, ar.duration)

        val success = db.update(TBL_AUDIO_RECORDS, contentValues, "id=" + ar.id, null)
        db.close()
        return success
    }

    fun deleteAudioRecordById(id: Int): Int{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, id)

        val success = db.delete(TBL_AUDIO_RECORDS, "id=$id", null)
        db.close()
        return success
    }

}