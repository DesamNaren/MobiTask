package com.example.mainactivity.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mainactivity.source.StatesData

@Dao
interface StateDao {

    @Query("DELETE FROM StatesData")
    fun deleteStates()

    @Insert
    fun insertState(statesData: List<StatesData?>?)

    @Query("SELECT COUNT(*) FROM statesdata")
    fun stateCount(): Int

    @Query("SELECT * from StatesData")
    fun getStatesData(): LiveData<List<StatesData>>

//    @Query("UPDATE work_locations SET address = :val WHERE id IN (:ss)")
//    int updateAllLocations(List<WorkLocationMaster> ss, String val);

}