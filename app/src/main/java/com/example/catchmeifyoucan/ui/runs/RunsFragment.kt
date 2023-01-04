package com.example.catchmeifyoucan.ui.runs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.activities.HomeActivity
import com.example.catchmeifyoucan.databinding.FragmentRunsBinding
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.utils.LoadingDialogSingle
import dagger.android.support.AndroidSupportInjection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class RunsFragment : BaseFragment() {

    companion object {
        private val TAG = RunsFragment::class.java.simpleName
        val RUN_ID = "run_id"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentRunsBinding
    private lateinit var viewModel: RunsFragmentViewModel

    private val runsListAdapter by lazy {
        RunsListAdapter {
            val args = bundleOf(RUN_ID to it.id)
            //TODO: navigate to run details
            findNavController().navigate(R.id.home_fragment)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRunsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[RunsFragmentViewModel::class.java]
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        initView()
        subscribe()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as HomeActivity).showToolbar(getString(R.string.runs))
    }

    private fun initView() {
        val dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.runsRecyclerView.addItemDecoration(dividerItemDecoration)
        binding.runsRecyclerView.adapter = runsListAdapter
    }

    private fun subscribe() {
        viewModel.getAllUserRuns()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(object :
                LoadingDialogSingle<List<RunsModel>>(requireActivity() as HomeActivity) {
                override fun onSuccess(t: List<RunsModel>) {
                    super.onSuccess(t)
                    runsListAdapter.submitList(null)
                    runsListAdapter.submitList(t)
                }
            })

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getAllUserRuns()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object :
                    LoadingDialogSingle<List<RunsModel>>(requireActivity() as HomeActivity) {
                    override fun onSuccess(t: List<RunsModel>) {
                        super.onSuccess(t)
                        runsListAdapter.submitList(null)
                        runsListAdapter.submitList(t)
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        Timber.e(e)
                    }
                })
        }
    }
}