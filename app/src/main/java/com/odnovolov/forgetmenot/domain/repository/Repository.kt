package com.odnovolov.forgetmenot.domain.repository

import com.odnovolov.forgetmenot.domain.entity.Deck

interface Repository {
    fun insertDeck(deck: Deck): Int
    fun getAllDeckNames(): List<String>
    fun saveDeckIdAsLastInserted(deckId: Int)
}