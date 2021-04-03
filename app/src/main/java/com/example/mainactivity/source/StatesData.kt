package com.example.mainactivity.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StatesData(

    val id:Int,
    @PrimaryKey
    val state_name: String,
    val fav:Boolean
)