package com.example.mainactivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mainactivity.application.MobiApplication

open class BaseActivity : AppCompatActivity() {
    lateinit var preferences: SharedPreferences
    lateinit var preferencesEditor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = MobiApplication.SharedPrefEditorObj.getPreferences()!!
        preferencesEditor = MobiApplication.SharedPrefEditorObj.getPreferencesEditor()!!
    }
}