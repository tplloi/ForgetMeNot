package com.odnovolov.forgetmenot.presentation.common.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseFragment : Fragment() {
    var viewCoroutineScope: CoroutineScope? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        super.onViewCreated(view, savedInstanceState)
    }

    inline fun <T> Flow<T>.observe(crossinline onEach: (value: T) -> Unit = {}) {
        viewCoroutineScope?.launch {
            collect {
                if (isActive) {
                    onEach(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        viewCoroutineScope!!.cancel()
        viewCoroutineScope = null
        super.onDestroyView()
    }
}