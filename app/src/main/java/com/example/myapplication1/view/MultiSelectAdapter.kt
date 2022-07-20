package com.example.myapplication1.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.R
import com.example.myapplication1.database.model.AudioRecordModel
import com.example.myapplication1.utils.OnItemClickListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MultiSelectAdapter (private var records : ArrayList<AudioRecordModel>, var listener: OnItemClickListener) : RecyclerView.Adapter<MultiSelectAdapter.ViewHolder>(){

    companion object{
        var itemSelectedList = mutableListOf<Int>()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{
        var tvFilename : TextView = itemView.findViewById(R.id.tvFilename)
        var tvDuration : TextView = itemView.findViewById(R.id.tvDuration)
        var tvTimestamp : TextView = itemView.findViewById(R.id.tvTimestamp)
        var checkBox : ImageButton = itemView.findViewById(R.id.checkbox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition

            if(itemSelectedList.contains(position)){
                itemSelectedList.remove(position)
                checkBox.setBackgroundResource(R.drawable.ic_uncheck)
            } else{
                itemSelectedList.add(position)
                checkBox.setBackgroundResource(R.drawable.ic_check)
            }

            if (position != RecyclerView.NO_POSITION)
            {
                listener.onItemClickListener(position)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_second, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION){
            val record = records[position]

            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val date = Date(record.timestamp.toLong() * 1000L)
            val strDate = sdf.format(date)

            holder.tvFilename.text = record.filename
            holder.tvTimestamp.text = strDate
            holder.tvDuration.text = record.duration

            if(itemSelectedList.contains(position)){
                holder.checkBox.setBackgroundResource(R.drawable.ic_check)
            } else{
                holder.checkBox.setBackgroundResource(R.drawable.ic_uncheck)
            }
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }
}