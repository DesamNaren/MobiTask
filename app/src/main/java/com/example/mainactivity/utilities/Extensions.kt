package com.example.mainactivity.utilities

import android.content.Context
import android.widget.Toast


object Extensions {
    /** Show Toast*/
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}


