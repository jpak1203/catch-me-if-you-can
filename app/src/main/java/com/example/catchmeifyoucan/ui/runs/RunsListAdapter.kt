package com.example.catchmeifyoucan.ui.runs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.RunListItemBinding
import com.example.catchmeifyoucan.utils.FormatUtil.getDate
import com.example.catchmeifyoucan.utils.FormatUtil.getRunTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class RunsListAdapter(private val showRunDetails: (RunsModel) -> Unit)
    : ListAdapter<RunsModel, RunsListAdapter.RunListViewHolder>(RunsDifferCallback()) {

    companion object {
        private val TAG = RunsListAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunListViewHolder {
        val binding = RunListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunListViewHolder, position: Int) {
        val runItem = getItem(position)
        holder.bind(runItem)
    }

    inner class RunListViewHolder(private val binding: RunListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(runs: RunsModel) {
            binding.runItem = runs
            binding.runTitle.text = getDate(runs.timeStamp)
            binding.runTime.text = String.format(binding.root.context.getString(R.string.run_time), getRunTime(runs.time))
            binding.stepCount.text = String.format(binding.root.context.getString(R.string.step_count), runs.stepCount)

            val storageRef = FirebaseStorage.getInstance().reference
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val pathReference = storageRef.child("${uid}/runImages/${runs.id}.jpg")

            Glide.with(binding.root)
                .load(pathReference)
                .into(binding.runMap)
        }
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