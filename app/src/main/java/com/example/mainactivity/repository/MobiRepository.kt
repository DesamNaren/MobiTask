package com.example.mainactivity.repository

import androidx.lifecycle.MutableLiveData
import com.cgg.virtuokotlin.network.getNetworkService
import com.example.mainactivity.BuildConfig
import com.example.mainactivity.source.StatesData
import com.example.mainactivity.source.TokenData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobiRepository {
    private val tokenRes = MutableLiveData<TokenData?>()
    private val statesRes = MutableLiveData<List<StatesData>?>()

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

    fun callStatesAPI(token: String): MutableLiveData<List<StatesData>?> {
        if (statesRes.value != null) {
            return statesRes
        }
        val vService = getNetworkService()
        val call = vService.getStatesAPI(token)

        call!!.enqueue(object : Callback<List<StatesData>> {
            override fun onResponse(
                call: Call<List<StatesData>?>,
                response: Response<List<StatesData>?>
            ) {
                statesRes.value = response.body()
            }

            override fun onFailure(call: Call<List<StatesData>?>, t: Throwable) {
                statesRes.value = null
            }

        })
        return statesRes
    }

}