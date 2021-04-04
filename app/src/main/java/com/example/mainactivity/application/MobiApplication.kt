package com.example.mainactivity.application

import android.app.Application
import android.content.SharedPreferences
import com.example.mainactivity.utilities.AppConstants

class MobiApplication : Application() {
    companion object {
        lateinit var instance: MobiApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    object SharedPrefEditorObj {
        fun getPreferences(): SharedPreferences? {
            return instance.getSharedPreferences(AppConstants.SHARED_PREF, MODE_PRIVATE);
        }

        fun getPreferencesEditor(): SharedPreferences.Editor? {
            return getPreferences()!!.edit();
        }
    }
}