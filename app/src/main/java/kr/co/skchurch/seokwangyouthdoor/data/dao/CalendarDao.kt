package kr.co.skchurch.seokwangyouthdoor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.skchurch.seokwangyouthdoor.data.entities.CalendarEntity

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar")
    fun getAllData(): List<CalendarEntity>

    @Query("SELECT * FROM calendar ORDER BY date ASC")
    fun getAllDataOrderByDate(): List<CalendarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: CalendarEntity)

    @Query("SELECT * FROM calendar WHERE id = :id")
    fun getDataById(id: Long): CalendarEntity

    @Query("SELECT * FROM calendar WHERE title = :title")
    fun getDataByTitle(title: String): List<CalendarEntity>

    @Query("SELECT * FROM calendar WHERE date = :date")
    fun getDataByDate(date: String): List<CalendarEntity>

    @Query("DELETE FROM calendar WHERE id = :id")
    fun deleteDataById(id: Long)

    @Query("DELETE FROM calendar WHERE title = :title")
    fun deleteDataByTitle(title: String)

    @Query("DELETE FROM calendar")
    fun deleteAllData()

    @Query("SELECT * FROM calendar WHERE uuid = :uuid")
    fun getDataByUUID(uuid: String): CalendarEntity

    @Query("DELETE FROM calendar WHERE uuid = :uuid")
    fun deleteDataByUUID(uuid: String)
}