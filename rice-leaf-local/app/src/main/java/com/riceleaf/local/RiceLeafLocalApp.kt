package com.riceleaf.local

import android.app.Application
import com.riceleaf.local.util.SettingsManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RiceLeafLocalApp : Application() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate() {
        super.onCreate()
        settingsManager.init()
    }
}
