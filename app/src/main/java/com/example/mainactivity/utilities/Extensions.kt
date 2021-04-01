package com.example.mainactivity.utilities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast


object Extensions {
    /** Show Toast*/
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

//    fun Context.firebaseToken(preferencesEditor: SharedPreferences.Editor) {
//        FirebaseInstallations.getInstance().getToken(false).addOnCompleteListener {
//            Log.e("FCM_TOKEN", it.result!!.token)
//            preferencesEditor.putString(AppConstants.FCM_TOKEN, it.result!!.token)
//            preferencesEditor.commit()
//        }
//    }

}


