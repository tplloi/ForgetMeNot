package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.persistence.shortterm.ExampleExerciseStateUseTimerProvider
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class ExampleExerciseDiScope private constructor(
    initialExerciseState: Exercise.State? = null,
    initialUseTimer: Boolean? = null
) {
    private val exerciseStateProvider = ExerciseStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        key = "ExampleExerciseState"
    )

    private val exerciseState: Exercise.State =
        initialExerciseState ?: exerciseStateProvider.load()

    private val useTimerProvider = ExampleExerciseStateUseTimerProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "ExampleExerciseState useTimer"
    )

    private val useTimer: Boolean =
        if (initialUseTimer != null) {
            useTimerProvider.save(initialUseTimer)
            initialUseTimer
        } else {
            useTimerProvider.load()
        }

    private val audioFocusManager = AudioFocusManager(
        AppDiScope.get().app
    )

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow,
        audioFocusManager
    )

    val exercise = ExampleExercise(
        exerciseState,
        useTimer,
        speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val controller = ExampleExerciseController(
        exercise,
        exerciseStateProvider
    )

    val viewModel = ExampleExerciseViewModel(
        exercise.state,
        useTimer,
        speakerImpl,
        AppDiScope.get().walkingModePreference,
        AppDiScope.get().globalState
    )

    private val offTestCardController = OffTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    private val manualTestCardController = ManualTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    private val quizTestCardController = QuizTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    private val entryTestCardController = EntryTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    fun getExerciseCardAdapter(
        coroutineScope: CoroutineScope
    ) = ExerciseCardAdapter(
        coroutineScope,
        offTestCardController,
        manualTestCardController,
        quizTestCardController,
        entryTestCardController
    )

    companion object : DiScopeManager<ExampleExerciseDiScope>() {
        fun create(
            initialExerciseState: Exercise.State,
            useTimer: Boolean
        ) = ExampleExerciseDiScope(
            initialExerciseState,
            useTimer
        )

        override fun recreateDiScope() = ExampleExerciseDiScope()

        override fun onCloseDiScope(diScope: ExampleExerciseDiScope) {
            with(diScope) {
                exercise.cancel()
                audioFocusManager.abandonAllRequests()
                speakerImpl.shutdown()
                controller.dispose()
                offTestCardController.dispose()
                manualTestCardController.dispose()
                quizTestCardController.dispose()
                entryTestCardController.dispose()
            }
        }
    }
}