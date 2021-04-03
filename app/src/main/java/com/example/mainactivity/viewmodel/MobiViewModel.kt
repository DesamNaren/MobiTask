package com.example.mainactivity.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainactivity.repository.MobiRepository
import com.example.mainactivity.source.StatesData
import com.example.mainactivity.source.TokenData

class MobiViewModel : ViewModel() {
    lateinit var sessionRes: MutableLiveData<TokenData?>
    lateinit var stateRes: MutableLiveData<List<StatesData>?>

    fun getSessionToken(): LiveData<TokenData?> {
        sessionRes = MobiRepository().callSessionAPI()
        return sessionRes
    }



}