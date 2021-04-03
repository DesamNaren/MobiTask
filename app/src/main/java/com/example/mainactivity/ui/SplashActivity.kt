package com.example.mainactivity.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mainactivity.R
import com.example.mainactivity.utilities.AppConstants
import com.example.mainactivity.utilities.Extensions.toast
import com.example.mainactivity.utilities.Utils
import com.example.mainactivity.viewmodel.MobiViewModel

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        callSessionAPI()
    }

    /** Call Session API*/
    private fun callSessionAPI() {
        when (Utils.checkInternetConnection(this)) {
            false -> toast("No Internet Connection")
            else -> {
                val viewModel: MobiViewModel =
                    ViewModelProvider(this).get(MobiViewModel::class.java)

                viewModel.getSessionToken().observe(this, Observer { tokenData ->
                    preferencesEditor.putString(AppConstants.SESSION_TOKEN, "Bearer " +tokenData!!.auth_token)
                    preferencesEditor.commit()
                    val newIntent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(newIntent)
                    finish()
                })
            }
        }
    }
}