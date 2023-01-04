package com.example.catchmeifyoucan.ui.runs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.RunListItemBinding
import java.util.*

class RunsListAdapter(private val showRunDetails: (RunsModel) -> Unit)
    : ListAdapter<RunsModel, RecyclerView.ViewHolder>(RunsDifferCallback()) {

    companion object {
        private val TAG = RunsListAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = RunListItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    ).run {
        this.onClick = showRunDetails
        root.tag = this
        object : RecyclerView.ViewHolder(root) {}
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int
    ) = getItem(position).let {
        (holder.itemView.tag as RunListItemBinding).apply {
            runItem = it
            runTitle.text = String.format(holder.itemView.context.getString(R.string.run_time), getShortDate(it.timeStamp))
            runTime.text = getRunTime(it.time)
            stepCount.text = String.format(holder.itemView.context.getString(R.string.step_count), it.stepCount)
        }.executePendingBindings()
    }

    class RunsDifferCallback: DiffUtil.ItemCallback<RunsModel>() {
        override fun areItemsTheSame(oldItem: RunsModel, newItem: RunsModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RunsModel, newItem: RunsModel): Boolean {
            return oldItem.id == newItem.id
        }

    }

    fun getShortDate(ts: String): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = ts.toLong()
        return android.text.format.DateFormat.format("E, dd MMM yyyy", calendar).toString()
    }

    fun getRunTime(seconds: Int): String {
        val hours: Int = seconds / 3600
        val minutes: Int = seconds % 3600 / 60
        val secs: Int = seconds % 60

        return java.lang.String.format(
            Locale.getDefault(),
            "%d:%02d:%02d", hours,
            minutes, secs
        )
    }

}