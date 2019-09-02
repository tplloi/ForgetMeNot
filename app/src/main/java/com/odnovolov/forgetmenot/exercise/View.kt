package com.odnovolov.forgetmenot.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.BaseFragment
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.exercise.ExerciseOrder.MoveToNextPosition
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardFragment
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import leakcanary.LeakSentry

class ExerciseFragment : BaseFragment() {

    private val controller = ExerciseController()
    private val viewModel = ExerciseViewModel()
    private lateinit var adapter: ExerciseCardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        takeOrders()
    }

    private fun setupView() {
        setupViewPagerAdapter()
        setupControlPanel()
    }

    private fun setupViewPagerAdapter() {
        adapter = ExerciseCardsAdapter(this)
        exerciseViewPager.adapter = adapter
        exerciseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                controller.dispatch(NewPageBecameSelected(position))
            }
        })
    }

    private fun setupControlPanel() {
        notAskButton.setOnClickListener { controller.dispatch(NotAskButtonClicked) }
        undoButton.setOnClickListener { controller.dispatch(UndoButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            adapter.cardIds = cardsIdsAtStart // we help ViewPager to restore its state
            cardIds.observe { adapter.cardIds = it }
            isCurrentCardLearned.observe { isCurrentCardLearned ->
                isCurrentCardLearned ?: return@observe
                notAskButton.visibility = if (isCurrentCardLearned) View.GONE else View.VISIBLE
                undoButton.visibility = if (isCurrentCardLearned) View.VISIBLE else View.GONE
            }
        }
    }

    private fun takeOrders() {
        viewScope!!.launch {
            for (order in controller.orders) {
                when (order) {
                    MoveToNextPosition -> {
                        val nextPosition = exerciseViewPager.currentItem + 1
                        exerciseViewPager.setCurrentItem(nextPosition, true)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}


class ExerciseCardsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    var cardIds: List<Long> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun createFragment(position: Int): Fragment {
        return ExerciseCardFragment.create(cardIds[position])
    }

    // this causes 'java.lang.IllegalStateException: Design assumption violated'
    //override fun getItemId(position: Int): Long = cardIds[position]

    override fun getItemCount(): Int = cardIds.size
}