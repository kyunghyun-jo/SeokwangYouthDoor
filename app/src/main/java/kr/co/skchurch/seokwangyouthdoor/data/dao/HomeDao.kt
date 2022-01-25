package kr.co.skchurch.seokwangyouthdoor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity

@Dao
interface HomeDao {
    @Query("SELECT * FROM home")
    fun getAllData(): List<HomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: HomeEntity)

    @Query("SELECT * FROM home WHERE id = :id")
    fun getDataById(id: Long): HomeEntity

    @Query("SELECT * FROM home WHERE title = :title")
    fun getDataByTitle(title: String): HomeEntity

    @Query("DELETE FROM home WHERE id = :id")
    fun deleteDataById(id: Long)

    @Query("DELETE FROM home WHERE title = :title")
    fun deleteDataByTitle(title: String)

    @Query("DELETE FROM home")
    fun deleteAllData()

    @Query("SELECT * FROM home WHERE uuid = :uuid")
    fun getDataByUUID(uuid: String): HomeEntity

    @Query("DELETE FROM home WHERE uuid = :uuid")
    fun deleteDataByUUID(uuid: String)
}