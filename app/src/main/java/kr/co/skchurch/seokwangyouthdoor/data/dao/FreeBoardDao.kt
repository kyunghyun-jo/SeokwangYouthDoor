package kr.co.skchurch.seokwangyouthdoor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.skchurch.seokwangyouthdoor.data.entities.FreeBoardEntity

@Dao
interface FreeBoardDao {
    @Query("SELECT * FROM freeBoard ORDER BY id DESC")
    fun getAllData(): List<FreeBoardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: FreeBoardEntity)

    @Query("SELECT * FROM freeBoard WHERE id = :id")
    fun getDataById(id: Long): FreeBoardEntity

    @Query("SELECT * FROM freeBoard WHERE author = :author")
    fun getDataByAuthor(author: String): List<FreeBoardEntity>

    @Query("DELETE FROM freeBoard WHERE id = :id")
    fun deleteDataById(id: Long)

    @Query("DELETE FROM freeBoard WHERE author = :author")
    fun deleteDataByAuthor(author: String)

    @Query("DELETE FROM freeBoard")
    fun deleteAllData()

    @Query("SELECT * FROM freeBoard WHERE uuid = :uuid")
    fun getDataByUUID(uuid: String): FreeBoardEntity

    @Query("DELETE FROM freeBoard WHERE uuid = :uuid")
    fun deleteDataByUUID(uuid: String)
}