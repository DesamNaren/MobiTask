package com.example.mainactivity.repository

import androidx.lifecycle.MutableLiveData
import com.cgg.virtuokotlin.network.getNetworkService
import com.example.mainactivity.BuildConfig
import com.example.mainactivity.source.TokenData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object MobiRepository {
    private val tokenRes = MutableLiveData<TokenData?>()

    fun callSessionAPI(): MutableLiveData<TokenData?> {
        if (tokenRes.value != null) {
            return tokenRes
        }
        val vService = getNetworkService()
        val call = vService.getSessionToken(BuildConfig.USER_EMAIL, BuildConfig.API_TOKEN)

        call!!.enqueue(object : Callback<TokenData?> {
            override fun onResponse(
                call: Call<TokenData?>,
                response: Response<TokenData?>
            ) {
                tokenRes.value = response.body()
            }

            override fun onFailure(call: Call<TokenData?>, t: Throwable) {
                tokenRes.value = null
            }

        })
        return tokenRes
    }
}