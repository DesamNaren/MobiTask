package com.example.mainactivity.interfaces

interface StatesInterface {
    fun stateCount(count: Int)
    fun checkCount(count: Int, name: String)
    fun stateFav(flag: Boolean, name:String, pos:Int)
    fun onItemClick(name:String)
}