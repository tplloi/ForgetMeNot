package com.odnovolov.forgetmenot.domain.feature.deckspreview

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.*
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable

class DecksPreviewFeature(
    repository: DeckRepository
) : BaseFeature<Wish, Action, Effect, State, Nothing>(
    initialState = State(),
    wishToAction = { wish -> wish },
    bootstrapper = BootstrapperImpl(repository),
    actor = ActorImpl(),
    reducer = ReducerImpl()
) {
    data class State(
        val decksPreview: List<DeckPreview> = emptyList()
    ) : Effect

    sealed class Wish : Action

    interface Action
    data class ProcessNewDecks(val decks: List<Deck>) : Action

    interface Effect

    class BootstrapperImpl(private val repository: DeckRepository) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.fromCallable { repository.loadAll() }
                .map { decks: List<Deck> -> ProcessNewDecks(decks) }
        }
    }

    class ActorImpl : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is ProcessNewDecks -> {
                    val decksPreview: List<DeckPreview> = action.decks
                        .map { deck: Deck -> DeckPreview(deck.id, deck.name) }
                        .toList()
                    return Observable.just(State(decksPreview))
                }
                else -> Observable.empty()
            }
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return effect as State
        }
    }
}