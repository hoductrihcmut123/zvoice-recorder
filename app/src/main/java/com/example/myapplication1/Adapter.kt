package com.example.myapplication1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Adapter(private var records : ArrayList<AudioRecordModel>) : RecyclerView.Adapter<Adapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var tvFilename : TextView = itemView.findViewById(R.id.tvFilename)
        var tvDuration : TextView = itemView.findViewById(R.id.tvDuration)
        var tvTimestamp : TextView = itemView.findViewById(R.id.tvTimestamp)
        var checkbox : CheckBox = itemView.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION){
            var record = records[position]

            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp.toLong() * 1000L)
            var strDate = sdf.format(date)

            holder.tvFilename.text = record.filename
            holder.tvTimestamp.text = strDate
            holder.tvDuration.text = record.duration
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }
}