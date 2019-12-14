package com.odnovolov.forgetmenot.screen.intervals

import com.odnovolov.forgetmenot.common.Interval
import com.odnovolov.forgetmenot.common.IntervalScheme
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.intervals.IntervalsViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IntervalsViewModel {
    private val queries: IntervalsViewModelQueries = database.intervalsViewModelQueries

    val intervalScheme: Flow<IntervalScheme?> = queries
        .getIntervalScheme()
        .asFlow()
        .mapToOneNotNull()

    val intervals: Flow<List<Interval>> = queries
        .getIntervals()
        .asFlow()
        .mapToList()

    val isRemoveIntervalButtonEnabled: Flow<Boolean> = intervals.map { it.size > 1 }
}