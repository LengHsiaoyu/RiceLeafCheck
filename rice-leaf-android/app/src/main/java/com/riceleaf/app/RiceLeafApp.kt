package com.riceleaf.app

import android.app.Application

class RiceLeafApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSettings.init(this)
    }
}
