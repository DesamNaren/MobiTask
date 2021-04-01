package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mainactivity.utilities.Extensions.toast
import com.example.mainactivity.utilities.Utils
import com.example.mainactivity.viewmodel.MobiViewModel

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        callSessionAPI()
    }

    /** Call Version API*/
    private fun callSessionAPI() {
        when (Utils.checkInternetConnection(this)) {
            false -> toast("No Internet Connection")
            else -> {
                val viewModel: MobiViewModel =
                    ViewModelProvider(this).get(MobiViewModel::class.java)

                viewModel.getSessionToken().observe(this, Observer { tokenData->
                    tokenData!!.auth_token
                })
            }
        }
    }
}