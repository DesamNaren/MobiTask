package com.example.mainactivity.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mainactivity.source.StatesData

@Dao
interface StateDao {

    @Query("DELETE FROM StatesData")
    fun deleteStates()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertState(statesData: List<StatesData?>?)

    @Query("SELECT COUNT(*) FROM statesdata")
    fun stateCount(): Int

    @Query("SELECT * from StatesData")
    fun getStatesData(): LiveData<List<StatesData>>

    @Query("UPDATE StatesData SET fav = :flag WHERE state_name LIKE :name")
    fun updateFav(flag: Boolean, name: String)


    @Query("UPDATE StatesData SET fav=0")
    fun resetFav()

    @Query("SELECT * from StatesData WHERE state_name LIKE :name")
    fun checkCity(name: String):LiveData<StatesData>


    @Query("DELETE FROM StatesData WHERE state_name LIKE :name")
    fun deleteItem(name: String):Int
}