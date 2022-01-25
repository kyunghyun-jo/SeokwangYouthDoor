package kr.co.skchurch.seokwangyouthdoor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.skchurch.seokwangyouthdoor.data.entities.TimetableEntity

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timeTable")
    fun getAllData(): List<TimetableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: TimetableEntity)

    @Query("SELECT * FROM timeTable WHERE id = :id")
    fun getDataById(id: Long): TimetableEntity

    @Query("SELECT * FROM timeTable WHERE title = :title")
    fun getDataByTitle(title: String): TimetableEntity

    @Query("DELETE FROM timeTable WHERE id = :id")
    fun deleteDataById(id: Long)

    @Query("DELETE FROM timeTable WHERE title = :title")
    fun deleteDataByTitle(title: String)

    @Query("DELETE FROM timeTable")
    fun deleteAllData()

    @Query("SELECT * FROM timeTable WHERE uuid = :uuid")
    fun getDataByUUID(uuid: String): TimetableEntity

    @Query("DELETE FROM timeTable WHERE uuid = :uuid")
    fun deleteDataByUUID(uuid: String)
}