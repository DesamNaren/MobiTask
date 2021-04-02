package com.example.mainactivity.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StatesData(
    @PrimaryKey
    val state_name: String
)