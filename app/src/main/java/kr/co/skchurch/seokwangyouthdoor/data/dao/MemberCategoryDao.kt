package kr.co.skchurch.seokwangyouthdoor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity

@Dao
interface MemberCategoryDao {
    @Query("SELECT * FROM simple")
    fun getAllData(): List<SimpleEntity>

    @Query("SELECT * FROM simple ORDER BY title ASC")
    fun getAllDataOrderByTitle(): List<SimpleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: SimpleEntity)

    @Query("SELECT * FROM simple WHERE id = :id")
    fun getDataById(id: Long): SimpleEntity

    @Query("SELECT * FROM simple WHERE title = :title")
    fun getDataByTitle(title: String): List<SimpleEntity>

    @Query("DELETE FROM simple WHERE id = :id")
    fun deleteDataById(id: Long)

    @Query("DELETE FROM simple WHERE title = :title")
    fun deleteDataByTitle(title: String)

    @Query("DELETE FROM simple")
    fun deleteAllData()

    @Query("SELECT * FROM simple WHERE uuid = :uuid")
    fun getDataByUUID(uuid: String): SimpleEntity

    @Query("DELETE FROM simple WHERE uuid = :uuid")
    fun deleteDataByUUID(uuid: String)
}