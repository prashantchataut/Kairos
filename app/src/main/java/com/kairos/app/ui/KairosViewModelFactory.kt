package com.kairos.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kairos.app.AppContainer
import com.kairos.app.ui.capsule.CapsuleViewModel
import com.kairos.app.ui.onboarding.OnboardingViewModel
import com.kairos.app.ui.studio.StudioViewModel
import com.kairos.app.ui.today.TodayViewModel
import com.kairos.app.ui.words.WordsViewModel
import com.kairos.app.ui.you.YouViewModel

class KairosViewModelFactory(
    private val container: AppContainer,
    private val appContext: Context,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(TodayViewModel::class.java) ->
            TodayViewModel(container.repository, container.clock) as T
        modelClass.isAssignableFrom(WordsViewModel::class.java) ->
            WordsViewModel(container.repository, container.clock) as T
        modelClass.isAssignableFrom(CapsuleViewModel::class.java) ->
            CapsuleViewModel(container.repository, container.attachments, container.clock) as T
        modelClass.isAssignableFrom(StudioViewModel::class.java) ->
            StudioViewModel(container.repository, container.clock, appContext) as T
        modelClass.isAssignableFrom(YouViewModel::class.java) ->
            YouViewModel(container.repository, container.clock) as T
        modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
            OnboardingViewModel(container.repository, container.clock) as T
        else -> error("No view model for ${modelClass.name}")
    }
}
