package com.odnovolov.forgetmenot.presentation.screen.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.string
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivityDiScope
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.settings.SettingsEvent.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.reflect.KProperty1

class SettingsFragment : BaseFragment() {
    init {
        MainActivityDiScope.reopenIfClosed()
        SettingsDiScope.reopenIfClosed()
    }

    private var controller: SettingsController? = null
    private lateinit var fullscreenModeDialog: Dialog
    private lateinit var fullscreenPreferenceAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initFullscreenModeDialog()
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun initFullscreenModeDialog() {
        fullscreenModeDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_fullscreen_mode_dialog),
            itemForm = AsCheckBox,
            onItemClick = { item: Item ->
                item as FullscreenPreferenceItem
                controller?.dispatch(
                    when (item.property) {
                        FullscreenPreference::isEnabledInDashboardAndSettings ->
                            FullscreenInDashboardAndSettingsCheckboxClicked
                        FullscreenPreference::isEnabledInExercise ->
                            FullscreenInExerciseCheckboxClicked
                        FullscreenPreference::isEnabledInRepetition ->
                            FullscreenInRepetitionCheckboxClicked
                        else -> throw AssertionError()
                    }
                )
            },
            takeAdapter = { fullscreenPreferenceAdapter = it }
        )
        dialogTimeCapsule.register("fullscreenModeDialog", fullscreenModeDialog)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = SettingsDiScope.getAsync()
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        walkingModeSettingsButton.setOnClickListener {
            controller?.dispatch(WalkingModeSettingsButton)
        }
        fullscreenSettingsSettingsButton.setOnClickListener {
            fullscreenModeDialog.show()
        }
    }

    @ExperimentalStdlibApi
    private fun observeViewModel(viewModel: SettingsViewModel) {
        with(viewModel) {
            fullscreenPreference.observe { fullscreenPreference: FullscreenPreference ->
                (requireActivity() as MainActivity).fullscreenModeManager
                    .setFullscreenMode(fullscreenPreference.isEnabledInDashboardAndSettings)

                val items = listOf(
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInDashboardAndSettings,
                        text = getString(R.string.item_text_fullscreen_in_dashboard_and_settings),
                        isSelected = fullscreenPreference.isEnabledInDashboardAndSettings
                    ),
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInExercise,
                        text = getString(R.string.item_text_fullscreen_in_exercise),
                        isSelected = fullscreenPreference.isEnabledInExercise
                    ),
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInRepetition,
                        text = getString(R.string.item_text_fullscreen_in_repetition),
                        isSelected = fullscreenPreference.isEnabledInRepetition
                    )
                )
                fullscreenPreferenceAdapter.submitList(items)

                fullscreenSettingsDescription.text = with(fullscreenPreference) {
                    when {
                        isEnabledInDashboardAndSettings
                                && isEnabledInExercise
                                && isEnabledInRepetition -> getString(string.everywhere)
                        !isEnabledInDashboardAndSettings
                                && !isEnabledInExercise
                                && !isEnabledInRepetition -> getString(string.nowhere)
                        else -> {
                            items.filter { item: Item -> item.isSelected }
                                .joinToString { item: Item ->
                                    item.text.toLowerCase(Locale.ROOT)
                                }.run { capitalize(Locale.ROOT) }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            SettingsDiScope.close()
        }
    }

    data class FullscreenPreferenceItem(
        val property: KProperty1<FullscreenPreference, Boolean>,
        override val text: String,
        override val isSelected: Boolean
    ) : Item
}