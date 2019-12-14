package com.odnovolov.forgetmenot.screen.intervals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.Interval
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.intervals.IntervalAdapter.ViewHolder
import com.odnovolov.forgetmenot.screen.intervals.IntervalsEvent.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.ShowModifyIntervalDialog
import com.odnovolov.forgetmenot.screen.intervals.modifyinterval.ModifyIntervalFragment
import kotlinx.android.synthetic.main.fragment_intervals.*
import kotlinx.android.synthetic.main.item_interval.view.*
import leakcanary.LeakSentry

class IntervalsFragment : BaseFragment() {
    private val controller = IntervalsController()
    private val viewModel = IntervalsViewModel()
    private val adapter = IntervalAdapter(controller)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intervals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.orders.forEach(execute = ::executeOrder)
    }

    private fun setupView() {
        intervalsRecyclerView.adapter = adapter
        addIntervalButton.setOnClickListener { controller.dispatch(AddIntervalButtonClicked) }
        removeIntervalButton.setOnClickListener { controller.dispatch(RemoveIntervalButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            intervalScheme.observe {
                intervalSchemeNameTextView.text = when {
                    it == null -> getString(R.string.off)
                    it.id == 0L -> getString(R.string.default_name)
                    it.name.isEmpty() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
                }
            }
            intervals.observe(onChange = adapter::submitList)
            isRemoveIntervalButtonEnabled.observe { isEnabled: Boolean ->
                removeIntervalButton.run { if (isEnabled) show() else hide() }
            }
        }
    }

    private fun executeOrder(order: IntervalsOrder) {
        when (order) {
            ShowModifyIntervalDialog -> {
                ModifyIntervalFragment().show(childFragmentManager, "ModifyIntervalFragment")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        intervalsRecyclerView.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}

class IntervalAdapter(private val controller: IntervalsController) :
    ListAdapter<Interval, ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interval, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            val interval: Interval = getItem(position)
            val backgroundRes = when (interval.targetLevelOfKnowledge) {
                1 -> R.drawable.background_level_of_knowledge_poor
                2 -> R.drawable.background_level_of_knowledge_acceptable
                3 -> R.drawable.background_level_of_knowledge_satisfactory
                4 -> R.drawable.background_level_of_knowledge_good
                5 -> R.drawable.background_level_of_knowledge_very_good
                else -> R.drawable.background_level_of_knowledge_excellent
            }
            levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
            levelOfKnowledgeTextView.text = interval.targetLevelOfKnowledge.toString()

            intervalTextView.text = interval.value
            modifyIntervalButton.setOnClickListener {
                controller.dispatch(
                    ModifyIntervalButtonClicked(interval.targetLevelOfKnowledge)
                )
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffCallback : DiffUtil.ItemCallback<Interval>() {
        override fun areItemsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.targetLevelOfKnowledge == newItem.targetLevelOfKnowledge
        }

        override fun areContentsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.targetLevelOfKnowledge == newItem.targetLevelOfKnowledge
                    && oldItem.value == newItem.value
        }
    }
}