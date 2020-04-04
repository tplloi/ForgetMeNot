package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.HomeScreenStateProvider.SerializableHomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.serialization.Serializable

class HomeScreenStateProvider(
    override val key: String = HomeScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<HomeScreenState, SerializableHomeScreenState>() {
    @Serializable
    data class SerializableHomeScreenState(
        val searchText: String,
        val selectedDeckIds: List<Long>
    )

    override val serializer = SerializableHomeScreenState.serializer()

    override fun toSerializable(state: HomeScreenState) = SerializableHomeScreenState(
        state.searchText,
        state.selectedDeckIds
    )

    override fun toOriginal(serializableState: SerializableHomeScreenState) =
        HomeScreenState().apply {
            searchText = serializableState.searchText
            selectedDeckIds = serializableState.selectedDeckIds
        }
}