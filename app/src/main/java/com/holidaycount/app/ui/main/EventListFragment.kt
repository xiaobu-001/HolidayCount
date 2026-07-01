package com.holidaycount.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.holidaycount.app.databinding.FragmentEventListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventListFragment : Fragment() {

    companion object {
        const val TYPE_UPCOMING = 0
        const val TYPE_ALL = 1
        const val TYPE_EXPIRED = 2
        private const val ARG_TYPE = "type"

        fun newInstance(type: Int): EventListFragment {
            val fragment = EventListFragment()
            fragment.arguments = Bundle().apply { putInt(ARG_TYPE, type) }
            return fragment
        }
    }

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: EventListAdapter
    private var listType: Int = TYPE_UPCOMING

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listType = arguments?.getInt(ARG_TYPE) ?: TYPE_UPCOMING

        adapter = EventListAdapter { event ->
            if (event.id > 0) {
                // 自定义事件 - 显示操作菜单
                showEventOptions(event)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@EventListFragment.adapter
        }

        observeEvents()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = when (listType) {
                    TYPE_UPCOMING -> viewModel.upcomingEvents
                    TYPE_ALL -> viewModel.allEvents
                    TYPE_EXPIRED -> viewModel.expiredEvents
                    else -> viewModel.allEvents
                }

                flow.collect { events ->
                    adapter.submitList(events)
                    binding.emptyView.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE

                    // 设置空状态文案
                    binding.tvEmpty.text = when (listType) {
                        TYPE_UPCOMING -> "暂无即将到来的事件\n点击 + 添加倒计时"
                        TYPE_EXPIRED -> "暂无已过期事件"
                        else -> "暂无事件"
                    }
                }
            }
        }
    }

    private fun showEventOptions(event: com.holidaycount.app.data.model.EventItem) {
        val options = arrayOf("编辑", "删除")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(event.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val dialog = com.holidaycount.app.ui.add.AddEventBottomSheet.newInstanceForEdit(event.id)
                        dialog.show(parentFragmentManager, "EditEventBottomSheet")
                    }
                    1 -> {
                        viewModel.deleteEvent(event.id)
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
