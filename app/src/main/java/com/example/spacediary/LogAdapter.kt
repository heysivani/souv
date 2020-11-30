package com.example.spacediary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(val logList: List<Logs>) :

    RecyclerView.Adapter<LogAdapter.LogVH>() {

    class LogVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText : TextView = itemView.findViewById(R.id.text_title)
        var memoText : TextView = itemView.findViewById(R.id.text_memo)
        var linearLayout : LinearLayout = itemView.findViewById(R.id.linearLayout)
        var expandableLayout : RelativeLayout = itemView.findViewById(R.id.expandable_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogVH {
        val view : View  = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)

        return LogVH(view)
    }

    override fun getItemCount(): Int {
        return logList.size
    }

    override fun onBindViewHolder(holder: LogVH, position: Int) {
        val logs: Logs = logList[position]
        holder.titleText.text = logs.title
        holder.memoText.text = logs.memo

        val isExpandable: Boolean = logList[position].expandable
        holder.expandableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

        holder.linearLayout.setOnClickListener{
            val logs = logList[position]
            logs.expandable = !logs.expandable
            notifyItemChanged(position)
        }
    }

}