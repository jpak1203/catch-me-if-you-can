package com.example.catchmeifyoucan.ui.runs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.RunListItemBinding
import com.example.catchmeifyoucan.utils.FormatUtil.getDate
import com.example.catchmeifyoucan.utils.FormatUtil.getRunTime

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
            runTitle.text = getDate(it.timeStamp)
            runTime.text = String.format(holder.itemView.context.getString(R.string.run_time), getRunTime(it.time))
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

}