package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class ExercisePreference(
    override val id: Long,
    name: String,
    randomOrder: Boolean,
    testMethod: TestMethod,
    intervalScheme: IntervalScheme?,
    pronunciation: Pronunciation,
    isQuestionDisplayed: Boolean,
    cardReverse: CardReverse,
    speakPlan: SpeakPlan,
    timeForAnswer: Int
) : RegistrableFlowableState<ExercisePreference>() {
    var name: String by me(name)
    var randomOrder: Boolean by me(randomOrder)
    var testMethod: TestMethod by me(testMethod)
    var intervalScheme: IntervalScheme? by me(intervalScheme)
    var pronunciation: Pronunciation by me(pronunciation)
    var isQuestionDisplayed: Boolean by me(isQuestionDisplayed)
    var cardReverse: CardReverse by me(cardReverse)
    var speakPlan: SpeakPlan by me(speakPlan)
    var timeForAnswer: Int by me(timeForAnswer)

    override fun copy() = ExercisePreference(
        id,
        name,
        randomOrder,
        testMethod,
        intervalScheme?.copy(),
        pronunciation.copy(),
        isQuestionDisplayed,
        cardReverse,
        speakPlan.copy(),
        timeForAnswer
    )

    companion object {
        val Default by lazy {
            ExercisePreference(
                id = 0L,
                name = "",
                randomOrder = true,
                testMethod = TestMethod.Manual,
                intervalScheme = IntervalScheme.Default,
                pronunciation = Pronunciation.Default,
                isQuestionDisplayed = true,
                cardReverse = CardReverse.Off,
                speakPlan = SpeakPlan.Default,
                timeForAnswer = 0 // that means 'do not use timer'
            )
        }
    }
}